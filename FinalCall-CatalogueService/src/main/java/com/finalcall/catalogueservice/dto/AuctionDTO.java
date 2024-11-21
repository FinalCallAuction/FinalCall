// src/main/java/com/finalcall/catalogueservice/dto/AuctionDTO.java

package com.finalcall.catalogueservice.dto;

import java.time.LocalDateTime;

public class AuctionDTO {
    private Long catalogueItemId; // Reference to Catalogue Service's item ID
    private String auctionType;
    private Double startingBidPrice;
    private LocalDateTime auctionEndTime;

    public AuctionDTO() {}

    public AuctionDTO(Long catalogueItemId, String auctionType, Double startingBidPrice, LocalDateTime auctionEndTime) {
        this.catalogueItemId = catalogueItemId;
        this.auctionType = auctionType;
        this.startingBidPrice = startingBidPrice;
        this.auctionEndTime = auctionEndTime;
    }

    public Long getCatalogueItemId() {
        return catalogueItemId;
    }

    public void setCatalogueItemId(Long catalogueItemId) {
        this.catalogueItemId = catalogueItemId;
    }

    public String getAuctionType() {
        return auctionType;
    }

    public void setAuctionType(String auctionType) {
        this.auctionType = auctionType;
    }

    public Double getStartingBidPrice() {
        return startingBidPrice;
    }

    public void setStartingBidPrice(Double startingBidPrice) {
        this.startingBidPrice = startingBidPrice;
    }

    public LocalDateTime getAuctionEndTime() {
        return auctionEndTime;
    }

    public void setAuctionEndTime(LocalDateTime auctionEndTime) {
        this.auctionEndTime = auctionEndTime;
    }
}
