package com.finalcall.auctionservice.dto;

import java.time.LocalDateTime;

public class NotificationDTO {
    private Long id; // Newly added
    private String message;
    private String type;  // Use an enum or string to categorize notifications
    private String link;  // Optional link for frontend navigation
    private LocalDateTime timestamp; // Newly added

    public NotificationDTO() {}

    public NotificationDTO(String message, String type) {
        this.message = message;
        this.type = type;
    }

    // Getters and Setters for all fields
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
