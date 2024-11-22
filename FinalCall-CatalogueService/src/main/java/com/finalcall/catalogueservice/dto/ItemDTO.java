package com.finalcall.catalogueservice.dto;

import java.util.List;

public class ItemDTO {
    private Long id;
    private String randomId;
    private String name;
    private String description;
    private String keywords;
    private List<String> imageUrls;
    private Long listedBy;
    private String listedByName; // New field for seller's name
    private Double startingBidPrice;
    private AuctionDTO auction;

    // Constructors
    public ItemDTO() {}

    public ItemDTO(Long id, String randomId, String name, String description, String keywords,
                   List<String> imageUrls, Long listedBy, String listedByName, Double startingBidPrice, AuctionDTO auction) {
        this.id = id;
        this.randomId = randomId;
        this.name = name;
        this.description = description;
        this.keywords = keywords;
        this.imageUrls = imageUrls;
        this.listedBy = listedBy;
        this.listedByName = listedByName;
        this.startingBidPrice = startingBidPrice;
        this.auction = auction;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public String getRandomId() {
        return randomId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getKeywords() {
        return keywords;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public Long getListedBy() {
        return listedBy;
    }

    public String getListedByName() {
        return listedByName;
    }

    public Double getStartingBidPrice() {
        return startingBidPrice;
    }

    public AuctionDTO getAuction() {
        return auction;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setRandomId(String randomId) {
        this.randomId = randomId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public void setListedBy(Long listedBy) {
        this.listedBy = listedBy;
    }

    public void setListedByName(String listedByName) {
        this.listedByName = listedByName;
    }

    public void setStartingBidPrice(Double startingBidPrice) {
        this.startingBidPrice = startingBidPrice;
    }

    public void setAuction(AuctionDTO auction) {
        this.auction = auction;
    }
    
}
