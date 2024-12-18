package com.finalcall.catalogueservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;

import com.finalcall.catalogueservice.handler.InternalWebSocketHandler;

import java.util.Map;
import java.util.Collections;

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
                    public boolean beforeHandshake(ServerHttpRequest request,
                                                   ServerHttpResponse response,
                                                   WebSocketHandler wsHandler,
                                                   Map<String, Object> attributes) throws Exception {
                        if (request instanceof ServletServerHttpRequest) {
                            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
                            
                            // Log all headers for debugging
                            servletRequest.getServletRequest().getHeaderNames().asIterator().forEachRemaining(headerName -> {
                                logger.info("Header: {} = {}", 
                                    headerName, 
                                    servletRequest.getServletRequest().getHeader(headerName)
                                );
                            });

                            String token = servletRequest.getServletRequest().getHeader("X-Internal-Token");
                            logger.info("Received X-Internal-Token: {}", token);

                            boolean isValid = "auth-catalogue-internal-token".equals(token);
                            logger.info("Token validation result: {}", isValid);

                            return isValid;
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
                })
                .setHandshakeHandler(new DefaultHandshakeHandler());
    }
}