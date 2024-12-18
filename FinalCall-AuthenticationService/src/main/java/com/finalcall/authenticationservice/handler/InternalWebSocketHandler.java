package com.finalcall.authenticationservice.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finalcall.authenticationservice.entity.User;
import com.finalcall.authenticationservice.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.Optional;
import java.util.HashMap;

@Component
public class InternalWebSocketHandler extends TextWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(InternalWebSocketHandler.class);

    private ObjectMapper objectMapper = new ObjectMapper();
    
    @Autowired
    private UserRepository userRepository;
    
    public InternalWebSocketHandler(ObjectMapper objectMapper, UserRepository userRepository) {
        this.objectMapper = objectMapper;
        this.userRepository = userRepository;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        logger.info("New WebSocket connection established: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            Map<String, Object> request = objectMapper.readValue(message.getPayload(), Map.class);
            String type = (String) request.get("type");
            String requestId = (String) request.get("requestId");
            Object data = request.get("data");

            Map<String, Object> response = new HashMap<>();
            response.put("requestId", requestId);

            logger.debug("Received WebSocket request - Type: {}, RequestId: {}, Data: {}", type, requestId, data);

            try {
                switch (type) {
                    case "user.getById":
                        Long userId = Long.valueOf(data.toString());
                        Optional<User> userOpt = userRepository.findById(userId);
                        if (userOpt.isPresent()) {
                            User user = userOpt.get();
                            Map<String, Object> userDetails = new HashMap<>();
                            userDetails.put("id", user.getId());
                            userDetails.put("username", user.getUsername());
                            userDetails.put("firstName", user.getFirstName());
                            userDetails.put("lastName", user.getLastName());
                            userDetails.put("email", user.getEmail());
                            userDetails.put("isSeller", user.getIsSeller());
                            response.put("data", userDetails);
                            logger.debug("User details retrieved successfully for ID: {}", userId);
                        } else {
                            response.put("error", "User not found");
                            logger.warn("User not found for ID: {}", userId);
                        }
                        break;
                    case "user.ping":
                        response.put("data", "pong");
                        break;
                    default:
                        response.put("error", "Unknown request type: " + type);
                        logger.warn("Unknown WebSocket request type: {}", type);
                }
            } catch (Exception e) {
                logger.error("Error processing WebSocket request", e);
                response.put("error", "Internal server error: " + e.getMessage());
            }

            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
        } catch (Exception e) {
            logger.error("Error handling WebSocket message", e);
        }
    }

    private Object getUserDetails(Long userId) {
        // Placeholder - replace with actual user service call
        Map<String, Object> userDetails = new HashMap<>();
        userDetails.put("id", userId);
        userDetails.put("username", "user" + userId);
        return userDetails;
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        logger.error("WebSocket transport error", exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        logger.info("WebSocket connection closed: {} with status {}", session.getId(), closeStatus);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}