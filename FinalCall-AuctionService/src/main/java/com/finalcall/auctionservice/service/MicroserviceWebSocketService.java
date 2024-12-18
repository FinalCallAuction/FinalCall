package com.finalcall.auctionservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.*;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
public class MicroserviceWebSocketService {
    private final WebSocketClient webSocketClient;
    private final ObjectMapper objectMapper;
    private final Map<String, WebSocketSession> sessions;
    private final Map<String, CompletableFuture<Object>> pendingRequests;

    public MicroserviceWebSocketService() {
        this.webSocketClient = new StandardWebSocketClient();
        this.objectMapper = new ObjectMapper();
        this.sessions = new ConcurrentHashMap<>();
        this.pendingRequests = new ConcurrentHashMap<>();
    }

    public void connectToCatalogueService() {
        connectToService("catalogue", "ws://localhost:8082/ws/internal");
    }

    public void connectToAuthService() {
        connectToService("auth", "ws://localhost:8081/ws/internal");
    }

    private void connectToService(String serviceName, String url) {
        webSocketClient.execute(
            new WebSocketHandler() {
                @Override
                public void afterConnectionEstablished(WebSocketSession session) {
                    sessions.put(serviceName, session);
                }

                @Override
                public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
                    try {
                        Map<String, Object> response = objectMapper.readValue(message.getPayload().toString(), Map.class);
                        String requestId = (String) response.get("requestId");
                        CompletableFuture<Object> future = pendingRequests.remove(requestId);
                        if (future != null) {
                            future.complete(response.get("data"));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void handleTransportError(WebSocketSession session, Throwable exception) {
                }

                @Override
                public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
                    sessions.remove(serviceName);
                }

                @Override
                public boolean supportsPartialMessages() {
                    return false;
                }
            },
            url
        );
    }

    public <T> CompletableFuture<T> sendRequest(String service, String type, Object data, Class<T> responseType) {
        try {
            WebSocketSession session = sessions.get(service);
            if (session == null || !session.isOpen()) {
                throw new IllegalStateException("No connection to " + service + " service");
            }

            String requestId = java.util.UUID.randomUUID().toString();
            Map<String, Object> request = Map.of(
                "requestId", requestId,
                "type", type,
                "data", data
            );

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