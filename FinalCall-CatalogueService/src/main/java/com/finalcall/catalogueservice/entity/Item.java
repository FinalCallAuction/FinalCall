package com.finalcall.catalogueservice.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "items")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 8)
    private String randomId;

    private String name;
    private String description;
    private String keywords;

    @ElementCollection
    @CollectionTable(name = "item_images", joinColumns = @JoinColumn(name = "item_id"))
    @Column(name = "image_url")
    private List<String> imageUrls = new ArrayList<>();

    @Column(name = "listed_by", nullable = false)
    private Long listedBy;

    @Column(name = "starting_bid_price")
    private Double startingBidPrice;

    public Item() {
        this.randomId = UUID.randomUUID().toString().substring(0, 8);
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) { 
		this.description = description;
	}

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) { 
        this.keywords = keywords;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public void addImageUrl(String imageUrl) {
        this.imageUrls.add(imageUrl);
    }

    public Long getListedBy() {
        return listedBy;
    }

    public void setListedBy(Long listedBy) {
        this.listedBy = listedBy;
    }

    public Double getStartingBidPrice() {
        return startingBidPrice;
    }

    public void setStartingBidPrice(Double startingBidPrice) {
        this.startingBidPrice = startingBidPrice;
    }
}
