package com.finalcall.authenticationservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finalcall.authenticationservice.dto.UserDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.*;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

@Service
public class MicroserviceWebSocketService {
    private static final Logger logger = LoggerFactory.getLogger(MicroserviceWebSocketService.class);

    private final StandardWebSocketClient webSocketClient;
    private final ObjectMapper objectMapper;
    private final Map<String, WebSocketSession> serviceSessions;
    private final Map<String, CompletableFuture<Object>> pendingRequests;
    
    private UserService userService;

    @Value("${websocket.service.tokens.catalogue:auth-catalogue-internal-token}")
    private String catalogueServiceToken;

    @Value("${websocket.service.urls.catalogue:ws://localhost:8082/ws/internal}")
    private String catalogueServiceUrl;

    public MicroserviceWebSocketService() {
        this.webSocketClient = new StandardWebSocketClient();
        this.objectMapper = new ObjectMapper();
        this.serviceSessions = new ConcurrentHashMap<>();
        this.pendingRequests = new ConcurrentHashMap<>();
    }

    public void connectToServices() {
        logger.info("Attempting to connect to services");
        logger.info("Catalogue Service Token: {}", catalogueServiceToken);
        logger.info("Catalogue Service URL: {}", catalogueServiceUrl);

        connectToCatalogueService();
    }

    private void connectToCatalogueService() {
        CompletableFuture.runAsync(() -> {
            try {
                URI uri = URI.create(catalogueServiceUrl);

                WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
                // Explicitly set X-Internal-Token, not Authorization
                headers.add("X-Internal-Token", catalogueServiceToken);
                logger.info("Connecting with token: {}", catalogueServiceToken);

                WebSocketHandler handler = new TextWebSocketHandler() {
                    // ... existing handler code ...
                };

                WebSocketSession session = webSocketClient.doHandshake(handler, headers, uri).get(10, TimeUnit.SECONDS);

                if (session.isOpen()) {
                    logger.info("WebSocket connection to Catalogue service established successfully");
                } else {
                    logger.error("WebSocket connection to Catalogue service failed to open");
                }
            } catch (Exception e) {
                logger.error("Failed to connect to Catalogue service", e);
            }
        });
    }

    private void connectToService(String serviceName, String url, String token) {
        try {
            logger.info("Attempting to connect to {} service at {}", serviceName, url);

            WebSocketHandler handler = new TextWebSocketHandler() {
                @Override
                public void afterConnectionEstablished(WebSocketSession session) {
                    logger.info("Connected to {} service successfully", serviceName);
                    serviceSessions.put(serviceName, session);
                }

                @Override
                protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
                    Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
                    String type = (String) payload.get("type");
                    String requestId = (String) payload.get("requestId");
                    Object data = payload.get("data");

                    Map<String, Object> response = new HashMap<>();
                    response.put("requestId", requestId);

                    try {
                        switch (type) {
                            case "user.getById":
                                Long userId = Long.valueOf(data.toString());
                                UserDTO userDTO = userService.getUserById(userId); 
                                // userService.getUserById should fetch user from authdb and return UserDTO
                                response.put("data", userDTO);
                                break;
                            default:
                                response.put("error", "Unknown request type: " + type);
                        }
                    } catch (Exception e) {
                        response.put("error", e.getMessage());
                    }

                    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
                }


                @Override
                public void handleTransportError(WebSocketSession session, Throwable exception) {
                    logger.error("Transport error for {} service: {}", serviceName, exception.getMessage(), exception);
                    serviceSessions.remove(serviceName);
                }

                @Override
                public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
                    logger.warn("{} service connection closed: {}", serviceName, status);
                    serviceSessions.remove(serviceName);
                }
            };

            // Create custom WebSocket headers
            WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
            
            // Add custom headers for authentication
            headers.add("X-Internal-Token", token);
            
            logger.info("Connecting to {} service with custom authentication", serviceName);

            WebSocketSession session = webSocketClient.execute(
                handler,
                headers,
                URI.create(url)
            ).get(10, TimeUnit.SECONDS);

            if (session.isOpen()) {
                logger.info("Successfully established WebSocket connection to {} service", serviceName);
            }
        } catch (Exception e) {
            logger.error("Failed to connect to {} service: {}", serviceName, e.getMessage(), e);
        }
    }
    
    public <T> CompletableFuture<T> sendRequest(String service, String type, Object data, Class<T> responseType) {
        try {
            WebSocketSession session = serviceSessions.get(service);
            if (session == null || !session.isOpen()) {
                CompletableFuture<T> failedFuture = new CompletableFuture<>();
                failedFuture.completeExceptionally(new IllegalStateException("No active WebSocket connection for " + service));
                return failedFuture;
            }

            String requestId = UUID.randomUUID().toString();
            Map<String, Object> request = Map.of(
                "requestId", requestId,
                "type", type,
                "data", data
            );

            CompletableFuture<Object> future = new CompletableFuture<>();
            pendingRequests.put(requestId, future);

            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(request)));

            return future.thenApply(response ->
                objectMapper.convertValue(response, responseType)
            );

        } catch (Exception e) {
            CompletableFuture<T> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(e);
            return failedFuture;
        }
    }

    public boolean isConnected(String serviceName) {
        WebSocketSession session = serviceSessions.get(serviceName);
        return session != null && session.isOpen();
    }
}