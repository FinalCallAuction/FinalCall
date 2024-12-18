package com.finalcall.authenticationservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistration;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;

import com.finalcall.auctionservice.websocket.ConsolidatedWebSocketHandler;
import com.finalcall.authenticationservice.handler.InternalWebSocketHandler;
import com.finalcall.authenticationservice.repository.UserRepository;

import java.util.Enumeration;
import java.util.Map;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);
    private final ConsolidatedWebSocketHandler consolidatedWebSocketHandler;

    public WebSocketConfig(ConsolidatedWebSocketHandler consolidatedWebSocketHandler) {
        this.consolidatedWebSocketHandler = consolidatedWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Add handler for auction-specific WebSocket connections
        registry.addHandler(consolidatedWebSocketHandler, "/ws/auctions/*")
               .setAllowedOrigins("*");

        // Add handler for internal service WebSocket connections
        registry.addHandler(consolidatedWebSocketHandler, "/ws/internal")
               .setAllowedOrigins("*")
               .addInterceptors(new HandshakeInterceptor() {
                    @Override
                    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, 
                                                 WebSocketHandler wsHandler, Map<String, Object> attributes) {
                        if (request instanceof ServletServerHttpRequest) {
                            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
                            String token = servletRequest.getServletRequest().getHeader("X-Internal-Token");
                            return "auth-catalogue-internal-token".equals(token) ||
                                   "auction-catalogue-internal-token".equals(token);
                        }
                        return false;
                    }

                    @Override
                    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                            WebSocketHandler wsHandler, Exception exception) {
                    }
               });
    }
}