package com.finalcall.auctionservice.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.finalcall.auctionservice.entity.AuctionType;

/**
 * Data Transfer Object for Auction.
 */
public class AuctionDTO {
    private Long id; // Added ID for reference
    private Long itemId;
    private AuctionType auctionType;
    private Double startingBidPrice;
    private Double currentBidPrice; // Newly added field
    private LocalDateTime auctionEndTime;
    private Long sellerId;
    private LocalDateTime startTime;
    private Double priceDecrement; // For DUTCH auctions
    private Double minimumPrice;    // For DUTCH auctions
    private Long currentBidderId;   // ID of the current highest bidder
    private List<String> imageUrls; // List of image URLs

    // Constructors
    public AuctionDTO() {}

    public AuctionDTO(Long id, Long itemId, AuctionType auctionType, Double startingBidPrice, Double currentBidPrice,
                     LocalDateTime auctionEndTime, Long sellerId, LocalDateTime startTime,
                     Double priceDecrement, Double minimumPrice, Long currentBidderId, List<String> imageUrls) {
        this.id = id;
        this.itemId = itemId;
        this.auctionType = auctionType;
        this.startingBidPrice = startingBidPrice;
        this.currentBidPrice = currentBidPrice;
        this.auctionEndTime = auctionEndTime;
        this.sellerId = sellerId;
        this.startTime = startTime;
        this.priceDecrement = priceDecrement;
        this.minimumPrice = minimumPrice;
        this.currentBidderId = currentBidderId;
        this.imageUrls = imageUrls;
    }

    // Getters and Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }

    public AuctionType getAuctionType() { return auctionType; }
    public void setAuctionType(AuctionType auctionType) { this.auctionType = auctionType; }

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

    public Double getPriceDecrement() { return priceDecrement; }
    public void setPriceDecrement(Double priceDecrement) { this.priceDecrement = priceDecrement; }

    public Double getMinimumPrice() { return minimumPrice; }
    public void setMinimumPrice(Double minimumPrice) { this.minimumPrice = minimumPrice; }

    public Long getCurrentBidderId() { return currentBidderId; }
    public void setCurrentBidderId(Long currentBidderId) { this.currentBidderId = currentBidderId; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    @Override
    public String toString() {
        return "AuctionDTO{" +
                "id=" + id +
                ", itemId=" + itemId +
                ", auctionType='" + auctionType + '\'' +
                ", startingBidPrice=" + startingBidPrice +
                ", currentBidPrice=" + currentBidPrice +
                ", auctionEndTime=" + auctionEndTime +
                ", sellerId=" + sellerId +
                ", startTime=" + startTime +
                ", priceDecrement=" + priceDecrement +
                ", minimumPrice=" + minimumPrice +
                ", currentBidderId=" + currentBidderId +
                ", imageUrls=" + imageUrls +
                '}';
    }
}
