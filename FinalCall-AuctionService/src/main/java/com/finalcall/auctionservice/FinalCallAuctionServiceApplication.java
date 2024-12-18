package com.finalcall.auctionservice;

import com.finalcall.auctionservice.service.MicroserviceWebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.context.ApplicationEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
@EnableScheduling
public class FinalCallAuctionServiceApplication {

    private static final Logger logger = LoggerFactory.getLogger(FinalCallAuctionServiceApplication.class);

    @Autowired
    private MicroserviceWebSocketService webSocketService;

    public static void main(String[] args) {
        SpringApplication.run(FinalCallAuctionServiceApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        logger.info("Application started, attempting initial WebSocket connections...");
        connectToServicesWithLogging();
    }

    // This method is called every 30 seconds to retry connections if not established.
    // It will only attempt a reconnect if one or both connections are down.
    @org.springframework.scheduling.annotation.Scheduled(fixedDelay = 30000)
    public void retryConnections() {
        boolean authConnected = webSocketService.isConnected("auth");
        boolean catalogueConnected = webSocketService.isConnected("catalogue");

        if (!authConnected || !catalogueConnected) {
            logger.info("Retrying WebSocket connections: authConnected={}, catalogueConnected={}", authConnected, catalogueConnected);
            connectToServicesWithLogging();
        }
    }

    private void connectToServicesWithLogging() {
        try {
            if (!webSocketService.isConnected("auth")) {
                logger.info("Connecting to Auth Service...");
                webSocketService.connectToAuthService();
            } else {
                logger.info("Already connected to Auth Service.");
            }

            if (!webSocketService.isConnected("catalogue")) {
                logger.info("Connecting to Catalogue Service...");
                webSocketService.connectToCatalogueService();
            } else {
                logger.info("Already connected to Catalogue Service.");
            }

        } catch (Exception e) {
            logger.error("Exception while attempting to connect to services", e);
        }
    }
}
