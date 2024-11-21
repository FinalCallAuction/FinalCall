// src/main/java/com/finalcall/auctionservice/config/WebSocketConfig.java

package com.finalcall.auctionservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finalcall.auctionservice.repository.AuctionRepository;
import com.finalcall.auctionservice.repository.BidRepository;
import com.finalcall.auctionservice.websocket.AuctionWSHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@ComponentScan("com.finalcall.auctionservice")
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private BidRepository bidRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(auctionHandler(), "/auctions/{auctionId}/update")
                .setAllowedOriginPatterns("*"); 
    }



    @Bean
    public WebSocketHandler auctionHandler() {
        return new AuctionWSHandler(auctionRepository, bidRepository, objectMapper);
    }
}
