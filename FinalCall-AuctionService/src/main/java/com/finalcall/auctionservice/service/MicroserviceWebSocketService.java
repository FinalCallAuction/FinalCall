package com.finalcall.auctionservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.*;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import org.springframework.http.HttpHeaders;
import org.springframework.web.socket.WebSocketHttpHeaders;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MicroserviceWebSocketService {
    private final WebSocketClient webSocketClient;
    private final ObjectMapper objectMapper;
    private final Map<String, WebSocketSession> sessions;
    private final Map<String, CompletableFuture<Object>> pendingRequests;

    @Value("${websocket.service.tokens.auth}")
    private String authServiceToken;

    @Value("${websocket.service.tokens.auction}")
    private String auctionServiceToken;

    public MicroserviceWebSocketService() {
        this.webSocketClient = new StandardWebSocketClient();
        this.objectMapper = new ObjectMapper();
        this.sessions = new ConcurrentHashMap<>();
        this.pendingRequests = new ConcurrentHashMap<>();
    }

    public void connectToCatalogueService() {
        try {
            connectToService("catalogue", "ws://localhost:8082/ws/internal", "auction-catalogue-internal-token");
        } catch (Exception e) {
            System.err.println("Failed to connect to Catalogue service: " + e.getMessage());
        }
    }

    public void connectToAuthService() {
        try {
            connectToService("auth", "ws://localhost:8081/ws/internal", "auth-catalogue-internal-token");
        } catch (Exception e) {
            System.err.println("Failed to connect to Auth service: " + e.getMessage());
        }
    }

    private void connectToService(String serviceName, String url, String internalToken) {
        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        headers.add("X-Internal-Token", internalToken);

        try {
            WebSocketSession session = webSocketClient.doHandshake(new AbstractWebSocketHandler() {

                @Override
                public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                    sessions.put(serviceName, session);
                    System.out.println("WebSocket connection to " + serviceName + " established");
                }

                @Override
                public void handleTextMessage(WebSocketSession session, TextMessage message) {
                    try {
                        Map<String, Object> response = objectMapper.readValue(message.getPayload(), Map.class);
                        String requestId = (String) response.get("requestId");

                        if (requestId == null) {
                            System.err.println("Received response without requestId: " + message.getPayload());
                            return;
                        }

                        CompletableFuture<Object> future = pendingRequests.remove(requestId);
                        if (future != null) {
                            if (response.containsKey("error")) {
                                future.completeExceptionally(new RuntimeException((String) response.get("error")));
                            } else {
                                future.complete(response.get("data"));
                            }
                        } else {
                            System.err.println("No pending request found for requestId: " + requestId);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void handleTransportError(WebSocketSession session, Throwable exception) {
                    sessions.remove(serviceName);
                }

                @Override
                public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
                    sessions.remove(serviceName);
                }

            }, headers, URI.create(url)).get();

            if (!session.isOpen()) {
                System.err.println("WebSocket connection to " + serviceName + " failed to open");
            }
        } catch (Exception e) {
            System.err.println("Error connecting to " + serviceName + " service at " + url);
            e.printStackTrace();
        }
    }

    public boolean isConnected(String serviceName) {
        WebSocketSession session = sessions.get(serviceName);
        return session != null && session.isOpen();
    }

    public <T> CompletableFuture<T> sendRequest(String service, String type, Object data, Class<T> responseType) {
        try {
            WebSocketSession session = sessions.get(service);
            if (session == null || !session.isOpen()) {
                throw new IllegalStateException("No connection to " + service + " service");
            }

            String requestId = UUID.randomUUID().toString();
            Map<String, Object> request = new HashMap<>();
            request.put("requestId", requestId);
            request.put("type", type);
            request.put("data", data);

            CompletableFuture<Object> future = new CompletableFuture<>();
            pendingRequests.put(requestId, future);

            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(request)));

            return future.thenApply(response -> objectMapper.convertValue(response, responseType));
        } catch (Exception e) {
            CompletableFuture<T> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
}
