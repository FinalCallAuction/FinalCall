// src/main/java/com/finalcall/auctionservice/FinalCallAuctionServiceApplication.java

package com.finalcall.auctionservice;

import com.finalcall.auctionservice.service.MicroserviceWebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
@EnableScheduling
public class FinalCallAuctionServiceApplication {

    @Autowired
    private MicroserviceWebSocketService webSocketService;

    public static void main(String[] args) {
        SpringApplication.run(FinalCallAuctionServiceApplication.class, args);
    }

}
