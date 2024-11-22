package com.finalcall.auctionservice.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

import com.finalcall.auctionservice.websocket.AuctionWSHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private AuctionWSHandler auctionHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(auctionHandler, "/auctions/{auctionId}/update")
                .setAllowedOriginPatterns("*") // Allow all origins; adjust as needed
                .withSockJS(); // Enable SockJS fallback options
    }
}
