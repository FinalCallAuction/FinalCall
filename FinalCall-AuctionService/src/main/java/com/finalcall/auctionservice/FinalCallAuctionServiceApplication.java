// src/main/java/com/finalcall/auctionservice/AuctionServiceApplication.java

package com.finalcall.auctionservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.finalcall.auctionservice.client")
@EnableScheduling
public class FinalCallAuctionServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(FinalCallAuctionServiceApplication.class, args);
    }
}

