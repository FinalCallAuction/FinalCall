// src/main/java/com/finalcall/auctionservice/entity/Auction.java

package com.finalcall.auctionservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing an Auction.
 */
@Entity
@Table(name = "auctions")
public class Auction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "catalogue_item_id", nullable = false, unique = true)
    private Long catalogueItemId;
    
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

    public Auction() {}

    public Auction(Long catalogueItemId, AuctionType auctionType, Double startingBidPrice, Double currentBidPrice, LocalDateTime auctionEndTime, Long sellerId, LocalDateTime startTime) {
        this.catalogueItemId = catalogueItemId;
        this.auctionType = auctionType;
        this.startingBidPrice = startingBidPrice;
        this.currentBidPrice = currentBidPrice;
        this.auctionEndTime = auctionEndTime;
        this.status = AuctionStatus.ACTIVE;
        this.sellerId = sellerId;
        this.startTime = startTime;
    }

    public Long getId() {
        return id;
    }

    public Long getCatalogueItemId() {
        return catalogueItemId;
    }

    public void setCatalogueItemId(Long catalogueItemId) {
        this.catalogueItemId = catalogueItemId;
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
