package com.finalcall.catalogueservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AuctionDTO {
    private Long id;
    private Long itemId;
    private String auctionType;
    private Double startingBidPrice;
    private Double currentBidPrice;
    private LocalDateTime auctionEndTime;
    private Long sellerId;
    private String sellerName;
    private LocalDateTime startTime;
    private Double priceDecrement;
    private Double minimumPrice;
    private Long currentBidderId;
    private List<String> imageUrls;
    private String status;
    private ItemDTO item;

    // Constructors
    public AuctionDTO() {}

    // Parameterized constructor
    public AuctionDTO(Long id, Long itemId, String auctionType, Double startingBidPrice,
                     Double currentBidPrice, LocalDateTime auctionEndTime, Long sellerId,
                     String sellerName, LocalDateTime startTime, Double priceDecrement,
                     Double minimumPrice, Long currentBidderId, List<String> imageUrls,
                     String status, ItemDTO item) {
        this.id = id;
        this.itemId = itemId;
        this.auctionType = auctionType;
        this.startingBidPrice = startingBidPrice;
        this.currentBidPrice = currentBidPrice;
        this.auctionEndTime = auctionEndTime;
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.startTime = startTime;
        this.priceDecrement = priceDecrement;
        this.minimumPrice = minimumPrice;
        this.currentBidderId = currentBidderId;
        this.imageUrls = imageUrls;
        this.status = status;
        this.item = item;
    }

    // Getters and Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public ItemDTO getItem() { return item; }
    public void setItem(ItemDTO item) { this.item = item; }

    /**
     * Computes the remaining time in seconds until the auction ends.
     * If the auction has already ended, returns 0.
     */
    @JsonProperty("timeLeft")
    public long getTimeLeft() {
        if (auctionEndTime == null) return 0;
        Duration duration = Duration.between(LocalDateTime.now(), auctionEndTime);
        return duration.isNegative() ? 0 : duration.getSeconds();
    }
}
