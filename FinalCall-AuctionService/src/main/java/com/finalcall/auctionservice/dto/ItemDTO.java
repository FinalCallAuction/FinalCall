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
    private String description; 
    private Long listedBy; 
    private List<String> imageUrls; 
    private String keywords;
    private Double startingBidPrice; 

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
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
    
    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public Double getStartingBidPrice() { 
        return startingBidPrice;
    }

    public void setStartingBidPrice(Double startingBidPrice) {
        this.startingBidPrice = startingBidPrice;
    }

}
