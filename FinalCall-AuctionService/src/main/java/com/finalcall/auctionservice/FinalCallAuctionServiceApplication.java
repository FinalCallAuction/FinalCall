// src/main/java/com/finalcall/auctionservice/AuctionServiceApplication.java

package com.finalcall.auctionservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for the FinalCall-AuctionService application.
 */
@SpringBootApplication
@EnableFeignClients // Enable Feign Clients for inter-service communication
@EnableScheduling // Enable scheduling for tasks like Dutch auction price decrement
public class FinalCallAuctionServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(FinalCallAuctionServiceApplication.class, args);
    }
}


