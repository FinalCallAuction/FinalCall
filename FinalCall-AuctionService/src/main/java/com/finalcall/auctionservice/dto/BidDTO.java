// src/main/java/com/finalcall/auctionservice/dto/BidDTO.java

package com.finalcall.auctionservice.dto;

import java.time.LocalDateTime;

public class BidDTO {
    private Long id;
    private Double amount;
    private Long bidderId;
    private String bidderUsername;
    private LocalDateTime timestamp;

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

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Long getBidderId() {
        return bidderId;
    }

    public void setBidderId(Long bidderId) {
        this.bidderId = bidderId;
    }

    public String getBidderUsername() {
        return bidderUsername;
    }

    public void setBidderUsername(String bidderUsername) {
        this.bidderUsername = bidderUsername;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
