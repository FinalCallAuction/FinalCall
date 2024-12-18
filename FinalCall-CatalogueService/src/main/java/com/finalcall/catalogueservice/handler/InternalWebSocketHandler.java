package com.finalcall.catalogueservice.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    public InternalWebSocketHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        logger.info("New WebSocket connection: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
            String type = (String) payload.get("type");
            String requestId = (String) payload.get("requestId");
            
            // Route messages based on type
            Object responseData = handleInternalRequest(type, payload.get("data"));
            
            // Send response
            Map<String, Object> response = Map.of(
                "requestId", requestId,
                "data", responseData
            );
            
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
        } catch (Exception e) {
            logger.error("Error processing WebSocket message", e);
        }
    }

    private Object handleInternalRequest(String type, Object data) {
        // Implement request routing logic
        switch (type) {
            case "catalogue.ping":
                return "pong";
            // Add more request types as needed
            default:
                throw new UnsupportedOperationException("Unsupported request type: " + type);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        logger.info("WebSocket connection closed: {} with status {}", session.getId(), status);
    }
}