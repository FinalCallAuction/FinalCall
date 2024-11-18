package com.finalcall.catalogueservice.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "items")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 8)
    private String randomId;

    private String name;

    @Column(precision = 19, scale = 4)
    private BigDecimal startingBid;

    @Column(precision = 19, scale = 4)
    private BigDecimal currentBid;

    @Enumerated(EnumType.STRING)
    private AuctionType auctionType;

    private LocalDateTime auctionEndTime;

    private String listedBy;

    private String imageUrl;

    private boolean sold;

    // Constructors
    public Item() {}

    public Item(String name, BigDecimal startingBid, AuctionType auctionType, LocalDateTime auctionEndTime, String listedBy, String imageUrl) {
        this.name = name;
        this.startingBid = startingBid;
        this.currentBid = startingBid;
        this.auctionType = auctionType;
        this.auctionEndTime = auctionEndTime;
        this.listedBy = listedBy;
        this.imageUrl = imageUrl;
        this.sold = false;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }

    public String getRandomId() {
        return randomId;
    }

    public void setRandomId(String randomId) {
        this.randomId = randomId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    
    public BigDecimal getStartingBid() {
        return startingBid;
    }

    public void setStartingBid(BigDecimal startingBid) {
        this.startingBid = startingBid;
    }

    public BigDecimal getCurrentBid() {
        return currentBid;
    }

    public void setCurrentBid(BigDecimal currentBid) {
        this.currentBid = currentBid;
    }

    public AuctionType getAuctionType() {
        return auctionType;
    }

    public void setAuctionType(AuctionType auctionType) {
        this.auctionType = auctionType;
    }

    public LocalDateTime getAuctionEndTime() {
        return auctionEndTime;
    }

    public void setAuctionEndTime(LocalDateTime auctionEndTime) {
        this.auctionEndTime = auctionEndTime;
    }

    public String getListedBy() {
        return listedBy;
    }

    public void setListedBy(String listedBy) {
        this.listedBy = listedBy;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public boolean isSold() {
        return sold;
    }

    public void setSold(boolean sold) {
        this.sold = sold;
    }
}
