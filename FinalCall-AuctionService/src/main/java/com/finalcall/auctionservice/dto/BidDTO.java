package com.finalcall.auctionservice.dto;

import java.time.LocalDateTime;

public class BidDTO {
    private Long id;
    private Double amount;
    private Long bidderId;
    private String bidderUsername;
    private LocalDateTime timestamp;
    private AuctionDTO auction;
    private String type; // "BID" or "PRICE_CHANGE"
    private Double previousAmount; // For showing price changes in Dutch auctions

    public BidDTO() {
    }

    public BidDTO(Long id, Double amount, Long bidderId, String bidderUsername, LocalDateTime timestamp) {
        this.id = id;
        this.amount = amount;
        this.bidderId = bidderId;
        this.bidderUsername = bidderUsername;
        this.timestamp = timestamp;
    }

    // Getters and Setters
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

    public void setAuction(AuctionDTO auction) {
        this.auction = auction;
    }

    public AuctionDTO getAuction() {
        return auction;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getPreviousAmount() {
        return previousAmount;
    }

    public void setPreviousAmount(Double previousAmount) {
        this.previousAmount = previousAmount;
    }
}