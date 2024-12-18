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
    private String listedByName; // Seller's name
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

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRandomId() { return randomId; }
    public void setRandomId(String randomId) { this.randomId = randomId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getKeywords() { return keywords; }
    public void setKeywords(String keywords) { this.keywords = keywords; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    public Long getListedBy() { return listedBy; }
    public void setListedBy(Long listedBy) { this.listedBy = listedBy; }

    public String getListedByName() { return listedByName; }
    public void setListedByName(String listedByName) { this.listedByName = listedByName; }

    public Double getStartingBidPrice() { return startingBidPrice; }
    public void setStartingBidPrice(Double startingBidPrice) { this.startingBidPrice = startingBidPrice; }

    public AuctionDTO getAuction() { return auction; }
    public void setAuction(AuctionDTO auction) { this.auction = auction; }
}
