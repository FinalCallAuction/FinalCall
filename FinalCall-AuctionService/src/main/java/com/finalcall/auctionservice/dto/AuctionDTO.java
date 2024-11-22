// src/main/java/com/finalcall/auctionservice/dto/AuctionDTO.java

package com.finalcall.auctionservice.dto;

import java.time.LocalDateTime;

import com.finalcall.auctionservice.entity.AuctionType;

public class AuctionDTO {
    private Long itemId;
    private AuctionType auctionType;
    private Double startingBidPrice;
    private Double currentBidPrice; // Newly added field
    private LocalDateTime auctionEndTime;
    private Long sellerId;
    private LocalDateTime startTime;

    // Constructors
    public AuctionDTO() {}

    public AuctionDTO(Long itemId, AuctionType auctionType, Double startingBidPrice, Double currentBidPrice,
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

    public AuctionType getAuctionType() { return auctionType; }
    public void setAuctionType(AuctionType auctionType) { this.auctionType = auctionType; }

    public Double getStartingBidPrice() { return startingBidPrice; }
    public void setStartingBidPrice(Double startingBidPrice) { this.startingBidPrice = startingBidPrice; }

    public LocalDateTime getAuctionEndTime() { return auctionEndTime; }
    public void setAuctionEndTime(LocalDateTime auctionEndTime) { this.auctionEndTime = auctionEndTime; }

    public Long getSellerId() { return sellerId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    
    public Double getCurrentBidPrice() { return currentBidPrice; }
    public void setCurrentBidPrice(Double currentBidPrice) { this.currentBidPrice = currentBidPrice; }
    
    

    @Override
    public String toString() {
        return "AuctionDTO{" +
                "itemId=" + itemId +
                ", auctionType='" + auctionType + '\'' +
                ", startingBidPrice=" + startingBidPrice +
                ", auctionEndTime=" + auctionEndTime +
                ", sellerId=" + sellerId +
                ", startTime=" + startTime +
                '}';
    }


}
