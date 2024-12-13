// src/main/java/com/finalcall/catalogueservice/dto/AuctionDTO.java

package com.finalcall.catalogueservice.dto;

import java.time.LocalDateTime;

public class AuctionDTO {
    private Long itemId;
    private String auctionType;
    private Double startingBidPrice;
    private Double currentBidPrice;
    private LocalDateTime auctionEndTime;
    private Long sellerId;
    private LocalDateTime startTime;

    // Constructors
    public AuctionDTO() {}

    public AuctionDTO(Long itemId, String auctionType, Double startingBidPrice, Double currentBidPrice,
                     LocalDateTime auctionEndTime, Long sellerId, LocalDateTime startTime) {
        this.itemId = itemId;
        this.auctionType = auctionType;
        this.startingBidPrice = startingBidPrice;
        this.currentBidPrice = currentBidPrice;
        this.auctionEndTime = auctionEndTime;
        this.sellerId = sellerId;
        this.startTime = startTime;
    }

    // Getters and Setters

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }

    public String getAuctionType() { return auctionType; }
    public void setAuctionType(String auctionType) { this.auctionType = auctionType; }

    public Double getStartingBidPrice() { return startingBidPrice; }
    public void setStartingBidPrice(Double startingBidPrice) { this.startingBidPrice = startingBidPrice; }

    public Double getCurrentBidPrice() { return currentBidPrice; }
    public void setCurrentBidPrice(Double currentBidPrice) { this.currentBidPrice = currentBidPrice; }

    public LocalDateTime getAuctionEndTime() { return auctionEndTime; }
    public void setAuctionEndTime(LocalDateTime auctionEndTime) { this.auctionEndTime = auctionEndTime; }

    public Long getSellerId() { return sellerId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    @Override
    public String toString() {
        return "AuctionDTO{" +
                "itemId=" + itemId +
                ", auctionType='" + auctionType + '\'' +
                ", startingBidPrice=" + startingBidPrice +
                ", currentBidPrice=" + currentBidPrice +
                ", auctionEndTime=" + auctionEndTime +
                ", sellerId=" + sellerId +
                ", startTime=" + startTime +
                '}';
    }
}
