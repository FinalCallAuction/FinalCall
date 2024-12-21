package com.finalcall.auctionservice.dto;

import com.finalcall.auctionservice.entity.AuctionType;
import java.time.LocalDateTime;
import java.util.List;

public class AuctionDTO {
    private Long id;
    private Long itemId;
    private AuctionType auctionType;
    private Double startingBidPrice;
    private Double currentBidPrice;
    private LocalDateTime auctionEndTime;
    private Long sellerId;
    private String sellerName;
    private LocalDateTime startTime;
    private Double priceDecrement;
    private Double minimumPrice;
    private Long currentBidderId;
    private String currentBidderName;
    private List<String> imageUrls;
    private String status;
    private ItemDTO item;
    private UserDTO currentBidderDetails;;
    private UserDTO sellerDetails;
    private LocalDateTime latestBidTimestamp;
    private Long totalBids;
    private Boolean isEnded;
    private String timeRemaining;

    // Existing getters and setters remain the same
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

    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public Double getPriceDecrement() { return priceDecrement; }
    public void setPriceDecrement(Double priceDecrement) { this.priceDecrement = priceDecrement; }

    public Double getMinimumPrice() { return minimumPrice; }
    public void setMinimumPrice(Double minimumPrice) { this.minimumPrice = minimumPrice; }

    public Long getCurrentBidderId() { return currentBidderId; }
    public void setCurrentBidderId(Long currentBidderId) { this.currentBidderId = currentBidderId; }

    public String getCurrentBidderName() { return currentBidderName; }
    public void setCurrentBidderName(String currentBidderName) { this.currentBidderName = currentBidderName; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public ItemDTO getItem() { return item; }
    public void setItem(ItemDTO item) { this.item = item; }
    
    public UserDTO getCurrentBidderDetails() {
        return currentBidderDetails;
    }

    public void setCurrentBidderDetails(UserDTO currentBidderDetails) {
        this.currentBidderDetails = currentBidderDetails;
    }


    public UserDTO getSellerDetails() {
        return sellerDetails;
    }

    public void setSellerDetails(UserDTO sellerDetails) {
        this.sellerDetails = sellerDetails;
    }

    public LocalDateTime getLatestBidTimestamp() {
        return latestBidTimestamp;
    }

    public void setLatestBidTimestamp(LocalDateTime latestBidTimestamp) {
        this.latestBidTimestamp = latestBidTimestamp;
    }

    public Long getTotalBids() {
        return totalBids;
    }

    public void setTotalBids(Long totalBids) {
        this.totalBids = totalBids;
    }

    public Boolean getIsEnded() {
        return isEnded;
    }

    public void setIsEnded(Boolean isEnded) {
        this.isEnded = isEnded;
    }

    public String getTimeRemaining() {return timeRemaining;}
    public void setTimeRemaining(String timeRemaining) {
        this.timeRemaining = timeRemaining;
    }
}