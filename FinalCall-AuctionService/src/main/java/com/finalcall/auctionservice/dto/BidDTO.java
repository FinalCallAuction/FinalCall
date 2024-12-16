package com.finalcall.auctionservice.dto;

import java.time.LocalDateTime;

public class BidDTO {
    private Long id;
    private Double amount;
    private Long bidderId;
    private String bidderUsername;
    private LocalDateTime timestamp;
    private AuctionDTO auction; // Add this field

    public BidDTO() {
    }

    public BidDTO(Long id, Double amount, Long bidderId, String bidderUsername, LocalDateTime timestamp) {
        this.id = id;
        this.amount = amount;
        this.bidderId = bidderId;
        this.bidderUsername = bidderUsername;
        this.timestamp = timestamp;
    }

    // Existing getters remain the same
    public Long getId() {
        return id;
    }

    public Double getAmount() {
        return amount;
    }

    public Long getBidderId() {
        return bidderId;
    }

    public String getBidderUsername() {
        return bidderUsername;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    // Add setters for all fields
    public void setId(Long id) {
        this.id = id;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public void setBidderId(Long bidderId) {
        this.bidderId = bidderId;
    }

    public void setBidderUsername(String bidderUsername) {
        this.bidderUsername = bidderUsername;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    // Setter and getter for auction
    public void setAuction(AuctionDTO auction) {
        this.auction = auction;
    }

    public AuctionDTO getAuction() {
        return auction;
    }
}