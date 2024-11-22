// src/main/java/com/finalcall/catalogueservice/dto/BidRequest.java

package com.finalcall.auctionservice.dto;

public class BidRequest {
    private Double bidAmount;
    private Long bidderId;

    // Constructors
    public BidRequest() {}

    public BidRequest(Double bidAmount, Long bidderId) {
        this.bidAmount = bidAmount;
        this.bidderId = bidderId;
    }

    // Getters and Setters
    public Double getBidAmount() {
        return bidAmount;
    }

    public void setBidAmount(Double bidAmount) {
        this.bidAmount = bidAmount;
    }

    public Long getBidderId() {
        return bidderId;
    }

    public void setBidderId(Long bidderId) {
        this.bidderId = bidderId;
    }
}
