package com.finalcall.auctionservice.config;

import com.finalcall.auctionservice.websocket.ConsolidatedWebSocketHandler;
import com.finalcall.auctionservice.websocket.InternalWebSocketHandler;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.*;
//src/main/java/com/finalcall/authenticationservice/config/WebSocketConfig.java
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
 private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);
 private final InternalWebSocketHandler internalWebSocketHandler;

 public WebSocketConfig(InternalWebSocketHandler internalWebSocketHandler) {
     this.internalWebSocketHandler = internalWebSocketHandler;
 }

 @Override
 public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
     registry.addHandler(internalWebSocketHandler, "/ws/internal")
             .setAllowedOrigins("*")
             .addInterceptors(new HandshakeInterceptor() {
                 @Override
                 public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
                     if (request instanceof ServletServerHttpRequest) {
                         ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
                         String token = servletRequest.getServletRequest().getHeader("X-Internal-Token");
                         logger.info("WebSocket Handshake Token: {}", token);
                         boolean tokenValid = "auth-catalogue-internal-token".equals(token) || "auction-catalogue-internal-token".equals(token);
                         logger.info("Token validation result: {}", tokenValid);

                         if (tokenValid) {
                             // Identify the service based on the token
                             String service = "unknown";
                             if ("auth-catalogue-internal-token".equals(token)) {
                                 service = "auth";
                             } else if ("auction-catalogue-internal-token".equals(token)) {
                                 service = "auction";
                             }
                             attributes.put("service", service);
                         }
                         return tokenValid;
                     }
                     return false;
                 }

                 @Override
                 public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
                     if (exception != null) {
                         logger.error("WebSocket Handshake failed", exception);
                     }
                 }
             })
             .setHandshakeHandler(new DefaultHandshakeHandler());
 }
}
