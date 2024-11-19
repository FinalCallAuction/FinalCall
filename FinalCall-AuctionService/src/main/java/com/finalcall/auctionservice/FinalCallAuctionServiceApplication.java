/**
 * The main entry point for the Spring Boot application.
 * Bootstraps the application and enables specific features.
 */
package com.finalcall.auctionservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FinalCallAuctionServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(FinalCallAuctionServiceApplication.class, args);
    }
}
