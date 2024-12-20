// src/main/java/com/finalcall/catalogueservice/dto/ItemRequest.java

package com.finalcall.catalogueservice.dto;

import java.time.LocalDateTime;

public class ItemRequest {
    private String name;
    private String description;
    private Double startingBid;
    private String auctionType;
    private LocalDateTime auctionStartTime;
    private LocalDateTime auctionEndTime;

    // Getters and Setters

    public String getName() {
        return name;
    }

    // Rest of the getters and setters

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    // Rest of the setters

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getStartingBid() {
        return startingBid;
    }

    public void setStartingBid(Double startingBid) {
        this.startingBid = startingBid;
    }

    public String getAuctionType() {
        return auctionType;
    }

    public void setAuctionType(String auctionType) {
        this.auctionType = auctionType;
    }

    public LocalDateTime getAuctionEndTime() {
        return auctionEndTime;
    }

    public void setAuctionEndTime(LocalDateTime auctionEndTime) {
        this.auctionEndTime = auctionEndTime;
    }

    public LocalDateTime getAuctionStartTime() {
        return auctionStartTime;
    }

    public void setAuctionStartTime(LocalDateTime auctionStartTime) {
        this.auctionStartTime = auctionStartTime;
    }
}
