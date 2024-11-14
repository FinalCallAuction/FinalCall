package com.finalcall.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "items")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 8)
    private String randomId; // New field for the 8-character ID

    private String name;
    private Double startingBid;
    private Double currentBid;

    @Enumerated(EnumType.STRING)
    private AuctionType auctionType;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime auctionEndTime;

    @ManyToOne
    @JoinColumn(name = "listed_by_id")
    private User listedBy;

    private String imageUrl;  // Existing field for storing image URL or path


    // Constructors
    public Item() {}

    public Item(String name, Double startingBid, AuctionType auctionType, LocalDateTime auctionEndTime, User listedBy, String imageUrl) {
        this.name = name;
        this.startingBid = startingBid;
        this.currentBid = startingBid; // Initial current bid is the starting bid
        this.auctionType = auctionType;
        this.auctionEndTime = auctionEndTime;
        this.listedBy = listedBy;
        this.imageUrl = imageUrl;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    public String getName() {
        return name;
    }

    public Double getStartingBid() {
        return startingBid;
    }

    public Double getCurrentBid() {
        return currentBid;
    }

    public AuctionType getAuctionType() {
        return auctionType;
    }

    public LocalDateTime getAuctionEndTime() {
        return auctionEndTime;
    }

    public User getListedBy() {
        return listedBy;
    }
    
    public String getRandomId() {
        return randomId;
    }

    public void setRandomId(String randomId) {
        this.randomId = randomId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStartingBid(Double startingBid) {
        this.startingBid = startingBid;
    }

    public void setCurrentBid(Double currentBid) {
        this.currentBid = currentBid;
    }

    public void setAuctionType(AuctionType auctionType) {
        this.auctionType = auctionType;
    }

    public void setAuctionEndTime(LocalDateTime auctionEndTime) {
        this.auctionEndTime = auctionEndTime;
    }

    public void setListedBy(User listedBy) {
        this.listedBy = listedBy;
    }
}
