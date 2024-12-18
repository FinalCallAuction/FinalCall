package com.finalcall.authenticationservice.config;

import com.finalcall.authenticationservice.service.MicroserviceWebSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class WebSocketInitializer {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketInitializer.class);
    
    private final MicroserviceWebSocketService webSocketService;

    public WebSocketInitializer(MicroserviceWebSocketService webSocketService) {
        this.webSocketService = webSocketService;
    }

    @Scheduled(initialDelay = 5000, fixedDelay = 30000)
    public void reconnectWebSockets() {
        try {
            logger.info("Attempting to reconnect WebSocket services");
            webSocketService.connectToServices();
        } catch (Exception e) {
            logger.error("WebSocket reconnection failed", e);
        }
    }
}