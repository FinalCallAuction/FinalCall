// src/main/java/com/finalcall/paymentservice/dto/ItemDTO.java
package com.finalcall.paymentservice.dto;

import java.util.List;

public class ItemDTO {
    private Long id;
    private String name;
    private List<String> imageUrls;
    private String description;

    public ItemDTO() {
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public String getDescription() {
        return description;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
