package com.finalcall.auctionservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing an Auction.
 */
@Entity
@Table(name = "auctions")
public class Auction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Reference to the Item ID from the CatalogueService
    @Column(name = "item_id", nullable = false, unique = true)
    private Long itemId;

    @Enumerated(EnumType.STRING)
    @Column(name = "auction_type", nullable = false)
    private AuctionType auctionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "auction_status", nullable = false)
    private AuctionStatus status;

    @Column(name = "starting_bid_price", nullable = false)
    private Double startingBidPrice;

    @Column(name = "current_bid_price", nullable = false)
    private Double currentBidPrice;

    @Column(name = "auction_end_time", nullable = false)
    private LocalDateTime auctionEndTime;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId; // User ID of the seller

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    
    @Column(name = "price_decrement")
    private Double priceDecrement; // For Dutch Auction

    @Column(name = "minimum_price")
    private Double minimumPrice; // For Dutch Auction

    @Column(name = "current_bidder_id")
    private Long currentBidderId; // ID of the current highest bidder

    @ElementCollection
    @CollectionTable(name = "auction_images", joinColumns = @JoinColumn(name = "auction_id"))
    @Column(name = "image_url")
    private List<String> imageUrls = new ArrayList<>();

    // Constructors
    public Auction() {
        this.status = AuctionStatus.ACTIVE; // Set default status
    }

    public Auction(Long itemId, AuctionType auctionType, Double startingBidPrice, Double currentBidPrice,
                  LocalDateTime auctionEndTime, Long sellerId, LocalDateTime startTime,
                  Double priceDecrement, Double minimumPrice) {
        this.itemId = itemId;
        this.auctionType = auctionType;
        this.startingBidPrice = startingBidPrice;
        this.currentBidPrice = currentBidPrice;
        this.auctionEndTime = auctionEndTime;
        this.sellerId = sellerId;
        this.startTime = startTime;
        this.priceDecrement = priceDecrement;
        this.minimumPrice = minimumPrice;
        this.status = AuctionStatus.ACTIVE;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public AuctionType getAuctionType() {
        return auctionType;
    }

    public void setAuctionType(AuctionType auctionType) {
        this.auctionType = auctionType;
    }

    public AuctionStatus getStatus() {
        return status;
    }

    public void setStatus(AuctionStatus status) {
        this.status = status;
    }

    public Double getStartingBidPrice() {
        return startingBidPrice;
    }

    public void setStartingBidPrice(Double startingBidPrice) {
        this.startingBidPrice = startingBidPrice;
    }

    public Double getCurrentBidPrice() {
        return currentBidPrice;
    }

    public void setCurrentBidPrice(Double currentBidPrice) {
        this.currentBidPrice = currentBidPrice;
    }

    public LocalDateTime getAuctionEndTime() {
        return auctionEndTime;
    }

    public void setAuctionEndTime(LocalDateTime auctionEndTime) {
        this.auctionEndTime = auctionEndTime;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public Double getPriceDecrement() {
        return priceDecrement;
    }

    public void setPriceDecrement(Double priceDecrement) {
        this.priceDecrement = priceDecrement;
    }

    public Double getMinimumPrice() {
        return minimumPrice;
    }

    public void setMinimumPrice(Double minimumPrice) {
        this.minimumPrice = minimumPrice;
    }

    public Long getCurrentBidderId() {
        return currentBidderId;
    }

    public void setCurrentBidderId(Long currentBidderId) {
        this.currentBidderId = currentBidderId;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    /**
     * Updates the auction status based on the current time and auction end time.
     */
    public void calculateStatus() {
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(this.auctionEndTime)) {
            if (this.status == AuctionStatus.ACTIVE) {
                this.status = AuctionStatus.ENDED;
            }
        }
    }
}
