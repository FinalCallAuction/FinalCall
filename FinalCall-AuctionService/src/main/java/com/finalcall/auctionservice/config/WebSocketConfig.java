package com.finalcall.auctionservice.config;

import com.finalcall.auctionservice.websocket.ConsolidatedWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final ConsolidatedWebSocketHandler auctionWebSocketHandler;

    public WebSocketConfig(ConsolidatedWebSocketHandler auctionWebSocketHandler) {
        this.auctionWebSocketHandler = auctionWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // External endpoints
        registry.addHandler(auctionWebSocketHandler, "/ws/auctions/{auctionId}")
               .setAllowedOrigins("http://localhost:3000");
        registry.addHandler(auctionWebSocketHandler, "/ws/items")
               .setAllowedOrigins("http://localhost:3000");
        registry.addHandler(auctionWebSocketHandler, "/ws/notifications/{userId}")
               .setAllowedOrigins("http://localhost:3000");
        
        // Internal endpoint
        registry.addHandler(auctionWebSocketHandler, "/ws/internal")
               .setAllowedOrigins("http://localhost:8081", "http://localhost:8082");
    }
}