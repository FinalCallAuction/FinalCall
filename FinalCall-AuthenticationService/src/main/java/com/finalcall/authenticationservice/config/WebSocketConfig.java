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

import com.finalcall.authenticationservice.handler.InternalWebSocketHandler;
import com.finalcall.authenticationservice.repository.UserRepository;

import java.util.Enumeration;
import java.util.Map;

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
        WebSocketHandlerRegistration registration = registry.addHandler(internalWebSocketHandler, "/ws/internal")
            .setAllowedOrigins("*");

        // Add more explicit configuration
        registration.addInterceptors(new HandshakeInterceptor() {
            @Override
            public boolean beforeHandshake(ServerHttpRequest request, 
                                           ServerHttpResponse response, 
                                           WebSocketHandler wsHandler, 
                                           Map<String, Object> attributes) throws Exception {
                if (request instanceof ServletServerHttpRequest) {
                    ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
                    
                    String token = servletRequest.getServletRequest().getHeader("X-Internal-Token");
                    logger.info("WebSocket Handshake Token: {}", token);
                    
                    boolean tokenValid = "auth-catalogue-internal-token".equals(token);
                    
                    if (tokenValid) {
                        // Explicitly set WebSocket upgrade headers
                        response.getHeaders().set("Upgrade", "websocket");
                        response.getHeaders().set("Connection", "Upgrade");
                    }
                    
                    return tokenValid;
                }
                return false;
            }

            @Override
            public void afterHandshake(ServerHttpRequest request, 
                                       ServerHttpResponse response, 
                                       WebSocketHandler wsHandler, 
                                       Exception exception) {
                if (exception != null) {
                    logger.error("WebSocket Handshake failed", exception);
                }
            }
        });
    }
}