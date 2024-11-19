// src/main/java/com/finalcall/auctionservice/dto/ItemDTO.java

package com.finalcall.auctionservice.dto;

import java.util.List;

/**
 * Data Transfer Object representing an item.
 * Used to transfer item data when interacting with the Catalogue Service.
 */
public class ItemDTO {
    private Long id;
    private String name;
    private String description; // Ensure this field is now present
    private Long listedBy; // Renamed from sellerId to listedBy
    private List<String> imageUrls; // Changed from String to List<String>
    private String keywords; // Ensure this field is now present
    private Double startingBidPrice; // Renamed from startingBid to startingBidPrice

    // Constructors
    public ItemDTO() {}

    public ItemDTO(Long id, String name, String description, Long listedBy, List<String> imageUrls, String keywords, Double startingBidPrice) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.listedBy = listedBy;
        this.imageUrls = imageUrls;
        this.keywords = keywords;
        this.startingBidPrice = startingBidPrice;
    }

    // Getters and setters for each field

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() { // Added
        return description;
    }

    public void setDescription(String description) { // Added
        this.description = description;
    }

    public Long getListedBy() {
        return listedBy;
    }

    public void setListedBy(Long listedBy) {
        this.listedBy = listedBy;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }
    
    public String getKeywords() { // Added
        return keywords;
    }

    public void setKeywords(String keywords) { // Added
        this.keywords = keywords;
    }

    public Double getStartingBidPrice() { // Added
        return startingBidPrice;
    }

    public void setStartingBidPrice(Double startingBidPrice) { // Added
        this.startingBidPrice = startingBidPrice;
    }

    // Optionally, override toString(), equals(), and hashCode() methods
}
