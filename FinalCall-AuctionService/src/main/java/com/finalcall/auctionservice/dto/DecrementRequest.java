package com.finalcall.auctionservice.dto;

public class DecrementRequest {
    private Long userId;
    private Double decrementAmount;

    // Getters and setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Double getDecrementAmount() { return decrementAmount; }
    public void setDecrementAmount(Double decrementAmount) { 
        this.decrementAmount = decrementAmount; 
    }
}