package com.finalcall.auctionservice.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finalcall.auctionservice.dto.AuctionDTO;
import com.finalcall.auctionservice.dto.NotificationDTO;
import com.finalcall.auctionservice.event.AuctionEvents.WebSocketEvent;
import com.finalcall.auctionservice.service.AuctionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ConsolidatedWebSocketHandler implements WebSocketHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<Long, Set<WebSocketSession>> auctionSessions = new ConcurrentHashMap<>();
    private final Map<Long, Set<WebSocketSession>> notificationSessions = new ConcurrentHashMap<>();
    private final Set<WebSocketSession> itemSessions = Collections.synchronizedSet(new HashSet<>());

    @Autowired
    private AuctionService auctionService;

    @Autowired
    private ObjectMapper sharedObjectMapper; // Injected ObjectMapper

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String uri = Objects.requireNonNull(session.getUri()).toString();
        if (uri.contains("/ws/auctions/")) {
            Long auctionId = extractId(uri, "/ws/auctions/");
            if (auctionId != null) {
                auctionSessions.computeIfAbsent(auctionId, k -> Collections.synchronizedSet(new HashSet<>())).add(session);
            }
        } else if (uri.contains("/ws/notifications/")) {
            Long userId = extractId(uri, "/ws/notifications/");
            if (userId != null) {
                notificationSessions.computeIfAbsent(userId, k -> Collections.synchronizedSet(new HashSet<>())).add(session);
            }
        } else if (uri.endsWith("/ws/items")) {
            itemSessions.add(session);
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
        // Handle incoming messages if needed
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        // Log error if needed
        exception.printStackTrace();
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        removeSessionFromMaps(session);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    @EventListener
    public void handleWebSocketEvent(WebSocketEvent event) {
        String type = event.getType();
        Object payload = event.getPayload();

        switch (type) {
            case "NEW_AUCTION":
                sendToItemSessions("NEW_AUCTION", payload);
                break;
            case "AUCTION_UPDATE":
                handleAuctionUpdate(event);
                break;
            default:
                break;
        }
    }

    public void sendToItemSessions(String type, Object payload) {
        sendMessageToSessions(itemSessions, type, payload);
    }

    public void sendToAuctionSessions(Long auctionId, String type, Object payload) {
        Set<WebSocketSession> sessions = auctionSessions.getOrDefault(auctionId, Collections.emptySet());
        sendMessageToSessions(sessions, type, payload);
    }

    public void sendNotificationToUser(Long userId, NotificationDTO notificationDTO) {
        Set<WebSocketSession> sessions = notificationSessions.getOrDefault(userId, Collections.emptySet());
        sendMessageToSessions(sessions, "NOTIFICATION", notificationDTO);
    }

    private void handleAuctionUpdate(WebSocketEvent event) {
        Map<String, Object> data = (Map<String, Object>) event.getPayload();
        AuctionDTO auctionDTO = (AuctionDTO) data.get("auction");
        if (auctionDTO != null && auctionDTO.getId() != null) {
            sendToAuctionSessions(auctionDTO.getId(), "AUCTION_UPDATE", data);
        }
    }

    private Long extractId(String uri, String prefix) {
        String[] parts = uri.split(prefix);
        if (parts.length > 1) {
            String idPart = parts[1].split("\\?")[0];
            try {
                return Long.valueOf(idPart);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private void removeSessionFromMaps(WebSocketSession session) {
        auctionSessions.values().forEach(set -> set.remove(session));
        notificationSessions.values().forEach(set -> set.remove(session));
        itemSessions.remove(session);
    }

    private void sendMessageToSessions(Collection<WebSocketSession> sessions, String type, Object payload) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", type);
        message.put("data", payload);

        try {
            String json = sharedObjectMapper.writeValueAsString(message);
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(new TextMessage(json));
                    } catch (IOException e) {
                        // Log error and possibly close session if needed
                        e.printStackTrace();
                    }
                }
            }
        } catch (JsonProcessingException e) {
            // Handle JSON processing error
            e.printStackTrace();
        }
    }
}
