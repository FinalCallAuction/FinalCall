package com.finalcall.auctionservice.dto;

import java.util.ArrayList;
import java.util.List;

public class BidResponse {
    private String message;
    private Double currentBidPrice;
    
    // New fields for notifications
    private List<NotificationDTO> notifications;
    private boolean hasNotifications;

    // Existing constructors, getters, and setters
    public BidResponse() {}

    public BidResponse(String message, Double currentBidPrice) {
        this.message = message;
        this.currentBidPrice = currentBidPrice;
    }

    // Add methods for notifications
    public void addNotification(NotificationDTO notification) {
        if (this.notifications == null) {
            this.notifications = new ArrayList<>();
        }
        this.notifications.add(notification);
        this.hasNotifications = true;
    }

    // Getters and setters for new fields
    public List<NotificationDTO> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<NotificationDTO> notifications) {
        this.notifications = notifications;
        this.hasNotifications = notifications != null && !notifications.isEmpty();
    }

    public boolean isHasNotifications() {
        return hasNotifications;
    }
}