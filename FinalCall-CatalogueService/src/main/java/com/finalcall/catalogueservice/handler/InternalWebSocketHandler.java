package com.finalcall.catalogueservice.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finalcall.catalogueservice.dto.ItemDTO;
import com.finalcall.catalogueservice.service.ItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;

@Component
public class InternalWebSocketHandler extends TextWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(InternalWebSocketHandler.class);

    private final ObjectMapper objectMapper;
    private final ItemService itemService;

    public InternalWebSocketHandler(ObjectMapper objectMapper, ItemService itemService) {
        this.objectMapper = objectMapper;
        this.itemService = itemService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        logger.info("New internal WebSocket connection: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
            String type = (String) payload.get("type");
            String requestId = (String) payload.get("requestId");
            Object data = payload.get("data");

            Object responseData = handleInternalRequest(type, data);

            Map<String, Object> response = Map.of(
                "requestId", requestId,
                "data", responseData
            );

            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
        } catch (Exception e) {
            logger.error("Error processing WebSocket message", e);

            // If an error occurs, send back an error response
            String errorMsg = e.getMessage();
            Map<String, Object> errorResponse = Map.of(
                "error", errorMsg
            );
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorResponse)));
        }
    }

    private Object handleInternalRequest(String type, Object data) throws Exception {
        switch (type) {
            case "catalogue.ping":
                return "pong";

            case "GET_ITEM":
                Long itemId = Long.valueOf(data.toString());
                ItemDTO itemDTO = itemService.getItemWithoutAuctionDetails(itemId);
                return itemDTO;

            default:
                throw new UnsupportedOperationException("Unsupported request type: " + type);
        }
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        logger.info("Internal WebSocket connection closed: {} with status {}", session.getId(), status);
    }
}
