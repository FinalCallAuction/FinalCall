package com.finalcall.auctionservice.dto;

import java.time.LocalDateTime;

public class BidResponse {
    private String message;
    private Double currentBidPrice;
    private String bidderName;
    private LocalDateTime bidTimestamp;
    private String auctionStatus;
    private Boolean isWinningBid;

    // Existing constructors...
    public BidResponse() {
    }

    public BidResponse(String message, Double currentBidPrice) {
        this.message = message;
        this.currentBidPrice = currentBidPrice;
    }

    // Add getters and setters for new fields
    public String getBidderName() {
        return bidderName;
    }

    public void setBidderName(String bidderName) {
        this.bidderName = bidderName;
    }

    public LocalDateTime getBidTimestamp() {
        return bidTimestamp;
    }

    public void setBidTimestamp(LocalDateTime bidTimestamp) {
        this.bidTimestamp = bidTimestamp;
    }

    public String getAuctionStatus() {
        return auctionStatus;
    }

    public void setAuctionStatus(String auctionStatus) {
        this.auctionStatus = auctionStatus;
    }

    public Boolean getIsWinningBid() {
        return isWinningBid;
    }

    public void setIsWinningBid(Boolean isWinningBid) {
        this.isWinningBid = isWinningBid;
    }

    // Keep existing getters and setters...
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