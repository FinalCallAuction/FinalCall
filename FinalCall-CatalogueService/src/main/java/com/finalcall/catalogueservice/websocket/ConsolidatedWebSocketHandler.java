package com.finalcall.catalogueservice.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finalcall.catalogueservice.service.ItemService;
import com.finalcall.catalogueservice.dto.ItemDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ConsolidatedWebSocketHandler implements WebSocketHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, WebSocketSession> internalServiceSessions = new ConcurrentHashMap<>();
    private final Set<WebSocketSession> itemUpdateSessions = Collections.synchronizedSet(new HashSet<>());

    @Autowired
    private ItemService itemService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String uri = Objects.requireNonNull(session.getUri()).toString();
        if (uri.contains("/ws/internal")) {
            internalServiceSessions.put(session.getId(), session);
        } else if (uri.contains("/ws/items")) {
            itemUpdateSessions.add(session);
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
        try {
            Map<String, Object> request = objectMapper.readValue(message.getPayload().toString(), Map.class);
            String type = (String) request.get("type");
            String requestId = (String) request.get("requestId");
            Object data = request.get("data");

            Map<String, Object> response = new HashMap<>();
            response.put("requestId", requestId);

            switch (type) {
                case "GET_ITEM":
                    handleGetItem(response, data);
                    break;
                default:
                    response.put("error", "Unknown request type");
            }

            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
        } catch (Exception e) {
            // Handle exception
        }
    }

    private void handleGetItem(Map<String, Object> response, Object data) {
        try {
            Long itemId = Long.valueOf(data.toString());
            ItemDTO itemDTO = itemService.getItemDetails(itemId);
            response.put("data", itemDTO);
        } catch (Exception e) {
            response.put("error", "Failed to fetch item: " + e.getMessage());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        // Handle transport error
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        internalServiceSessions.remove(session.getId());
        itemUpdateSessions.remove(session);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    public void broadcastItemUpdate(String type, Object payload) {
        sendMessageToSessions(itemUpdateSessions, type, payload);
    }

    private void sendMessageToSessions(Collection<WebSocketSession> sessions, String type, Object payload) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", type);
            message.put("data", payload);
            String jsonMessage = objectMapper.writeValueAsString(message);

            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(jsonMessage));
                }
            }
        } catch (Exception e) {
            // Handle exception
        }
    }
}