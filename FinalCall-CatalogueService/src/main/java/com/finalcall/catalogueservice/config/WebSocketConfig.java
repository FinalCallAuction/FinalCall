package com.finalcall.catalogueservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.finalcall.catalogueservice.websocket.ConsolidatedWebSocketHandler;

import java.util.Map;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private ConsolidatedWebSocketHandler consolidatedWebSocketHandler;
    
    @Autowired
    private InternalWebSocketHandler internalWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Public endpoints for client connections
        registry.addHandler(consolidatedWebSocketHandler, "/ws/items")
               .setAllowedOrigins("*");

        // Internal service endpoints
        registry.addHandler(internalWebSocketHandler, "/ws/internal")
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