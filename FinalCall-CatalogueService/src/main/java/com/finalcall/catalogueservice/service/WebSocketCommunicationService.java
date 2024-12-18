package com.finalcall.catalogueservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
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
public class WebSocketCommunicationService {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketCommunicationService.class);
    private final WebSocketClient webSocketClient;
    private final Map<String, WebSocketSession> serviceSessions = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<Object>> pendingRequests = new ConcurrentHashMap<>();

    @Value("${websocket.service.urls.auth}")
    private String authServiceUrl;

    @Value("${websocket.service.urls.auction}")
    private String auctionServiceUrl;

    @Value("${websocket.service.tokens.auth}")
    private String authServiceToken;

    @Value("${websocket.service.tokens.auction}")
    private String auctionServiceToken;

    @Autowired
    private ObjectMapper objectMapper; // use the bean from JacksonConfig

    public WebSocketCommunicationService() {
        this.webSocketClient = new StandardWebSocketClient();
    }

    @PostConstruct
    public void init() {
        logger.info("Initializing WebSocket Communication Service");
        logger.info("Auth Service URL: {}", authServiceUrl);
        logger.info("Auction Service URL: {}", auctionServiceUrl);
        logger.info("Auth Service Token: {}", authServiceToken);
        logger.info("Auction Service Token: {}", auctionServiceToken);
        connectToServices();
    }


    public void connectToServices() {
        connectToAuthService();
        connectToAuctionService();
    }

    private void connectToAuthService() {
        CompletableFuture.runAsync(() -> {
            try {
                URI uri = URI.create(authServiceUrl);
                WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
                headers.add("X-Internal-Token", authServiceToken);
                logger.info("Attempting WebSocket connection to Auth Service at: {}", uri);

                WebSocketHandler handler = new TextWebSocketHandler() {
                    @Override
                    public void afterConnectionEstablished(WebSocketSession session) {
                        logger.info("WebSocket Connection Established to Auth Service");
                        serviceSessions.put("auth", session);
                    }

                    @Override
                    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
                        logger.info("Received WebSocket Message from Auth Service: {}", message.getPayload());
                        processIncomingMessage(message.getPayload(), "auth");
                    }

                    @Override
                    public void handleTransportError(WebSocketSession session, Throwable exception) {
                        logger.error("WebSocket Transport Error with Auth Service", exception);
                        serviceSessions.remove("auth");
                    }

                    @Override
                    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
                        logger.warn("WebSocket connection closed with Auth Service: {}", status);
                        serviceSessions.remove("auth");
                    }
                };

                WebSocketSession session = webSocketClient.doHandshake(handler, headers, uri)
                        .get(15, TimeUnit.SECONDS); // Increased timeout

                if (session.isOpen()) {
                    logger.info("WebSocket Connection to Auth Service: SUCCESS");
                } else {
                    logger.error("WebSocket Connection to Auth Service: FAILED TO OPEN");
                }
            } catch (Exception e) {
                logger.error("WebSocket Connection Error with Auth Service", e);
            }
        });
    }

    private void connectToAuctionService() {
        CompletableFuture.runAsync(() -> {
            try {
                URI uri = URI.create(auctionServiceUrl);
                WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
                headers.add("X-Internal-Token", auctionServiceToken);
                logger.info("Attempting WebSocket connection to Auction Service at: {}", uri);

                WebSocketHandler handler = new TextWebSocketHandler() {
                    @Override
                    public void afterConnectionEstablished(WebSocketSession session) {
                        logger.info("WebSocket Connection Established to Auction Service");
                        serviceSessions.put("auction", session);
                    }

                    @Override
                    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
                        logger.info("Received WebSocket Message from Auction Service: {}", message.getPayload());
                        processIncomingMessage(message.getPayload(), "auction");
                    }

                    @Override
                    public void handleTransportError(WebSocketSession session, Throwable exception) {
                        logger.error("WebSocket Transport Error with Auction Service", exception);
                        serviceSessions.remove("auction");
                    }

                    @Override
                    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
                        logger.warn("WebSocket connection closed with Auction Service: {}", status);
                        serviceSessions.remove("auction");
                    }
                };

                WebSocketSession session = webSocketClient.doHandshake(handler, headers, uri)
                        .get(15, TimeUnit.SECONDS); // Increased timeout

                if (session.isOpen()) {
                    logger.info("WebSocket Connection to Auction Service: SUCCESS");
                } else {
                    logger.error("WebSocket Connection to Auction Service: FAILED TO OPEN");
                }
            } catch (Exception e) {
                logger.error("WebSocket Connection Error with Auction Service", e);
            }
        });
    }

    /**
     * Processes incoming WebSocket messages and completes pending requests.
     *
     * @param payload The JSON payload received.
     * @param service The service identifier ("auth" or "auction").
     */
    private void processIncomingMessage(String payload, String service) {
        try {
            logger.info("Received message from {} service: {}", service, payload);
            Map<String, Object> messageMap = objectMapper.readValue(payload, Map.class);
            String requestId = (String) messageMap.get("requestId");
            Object data = messageMap.get("data");
            String error = (String) messageMap.get("error");

            if (requestId == null) {
                logger.warn("Received message without requestId from {} service: {}", service, payload);
                return;
            }

            CompletableFuture<Object> future = pendingRequests.remove(requestId);
            if (future == null) {
                logger.warn("No pending request found for requestId: {} from service: {}", requestId, service);
                return;
            }

            if (error != null) {
                logger.error("Error response from {} service for request {}: {}", service, requestId, error);
                future.completeExceptionally(new RuntimeException(error));
            } else {
                logger.info("Successful response from {} service for request {}", service, requestId);
                future.complete(data);
            }
        } catch (Exception e) {
            logger.error("Error processing incoming WebSocket message from {} service: {}", service, e.getMessage());
            logger.error("Full error details:", e);
        }
    }

    /**
     * Sends a request over WebSocket to the specified service.
     *
     * @param service      The target service ("auth" or "auction").
     * @param type         The type of request.
     * @param data         The data payload.
     * @param responseType The expected response type.
     * @param <T>          The type parameter.
     * @return A CompletableFuture that will be completed with the response.
     */
    public <T> CompletableFuture<T> sendRequest(String service, String type, Object data, Class<T> responseType) {
        try {
            WebSocketSession session = serviceSessions.get(service);
            logger.info("Attempting to send request to {} service. Session exists: {}, Session open: {}", 
                service, 
                session != null, 
                session != null ? session.isOpen() : false);

            if (session == null || !session.isOpen()) {
                logger.error("No active WebSocket connection for {} service", service);
                CompletableFuture<T> failedFuture = new CompletableFuture<>();
                failedFuture.completeExceptionally(
                    new IllegalStateException("No active WebSocket connection for " + service)
                );
                return failedFuture;
            }

            String requestId = UUID.randomUUID().toString();
            Map<String, Object> request = new HashMap<>();
            request.put("requestId", requestId);
            request.put("type", type);
            request.put("data", data);

            CompletableFuture<Object> future = new CompletableFuture<>();
            pendingRequests.put(requestId, future);

            String requestJson = objectMapper.writeValueAsString(request);
            logger.info("Sending WebSocket request to {} service (ID: {}): {}", service, requestId, requestJson);
            
            session.sendMessage(new TextMessage(requestJson));
            
            return future.thenApply(response -> {
                logger.info("Processing response for request {} from {} service", requestId, service);
                return objectMapper.convertValue(response, responseType);
            });
        } catch (Exception e) {
            logger.error("Error sending WebSocket request to {} service", service, e);
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