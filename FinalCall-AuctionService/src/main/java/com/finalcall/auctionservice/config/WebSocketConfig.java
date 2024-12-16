// src/main/java/com/finalcall/auctionservice/config/WebSocketConfig.java

package com.finalcall.auctionservice.config;

import com.finalcall.auctionservice.websocket.AuctionWSHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

/**
 * Configures WebSocket endpoints and handlers.
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final AuctionWSHandler auctionWSHandler;

    public WebSocketConfig(AuctionWSHandler auctionWSHandler) {
        this.auctionWSHandler = auctionWSHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(auctionWSHandler, "/ws/auctions/{auctionId}")
                .setAllowedOrigins("*"); // Adjust allowed origins as necessary
    }
}
