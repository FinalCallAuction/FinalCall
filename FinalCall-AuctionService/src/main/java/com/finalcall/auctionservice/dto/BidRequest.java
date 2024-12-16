// src/main/java/com/finalcall/auctionservice/dto/BidRequest.java

package com.finalcall.auctionservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class BidRequest {
    @NotNull(message = "Bid amount is required.")
    @Min(value = 0, message = "Bid amount must be positive.")
    private Double bidAmount;

    @NotNull(message = "Bidder ID is required.")
    private Long bidderId;

    public BidRequest() {
    }

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
