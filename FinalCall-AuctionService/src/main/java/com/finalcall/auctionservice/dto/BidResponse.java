// src/main/java/com/finalcall/catalogueservice/dto/BidResponse.java

package com.finalcall.auctionservice.dto;

public class BidResponse {
    private String message;
    private Double currentBidPrice;

    // Constructors
    public BidResponse() {}

    public BidResponse(String message, Double currentBidPrice) {
        this.message = message;
        this.currentBidPrice = currentBidPrice;
    }

    // Getters and Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Double getCurrentBidPrice() {
        return currentBidPrice;
    }

    public void setCurrentBidPrice(Double currentBidPrice) {
        this.currentBidPrice = currentBidPrice;
    }
}
