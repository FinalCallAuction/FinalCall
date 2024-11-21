// src/main/java/com/finalcall/auctionservice/FinalCallAuctionServiceApplication.java

package com.finalcall.auctionservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.finalcall.auctionservice.services") 
public class FinalCallAuctionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinalCallAuctionServiceApplication.class, args);
    }
}
