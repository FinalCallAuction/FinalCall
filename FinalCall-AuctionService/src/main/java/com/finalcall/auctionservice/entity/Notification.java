// src/main/java/com/finalcall/auctionservice/entity/Notification.java

package com.finalcall.auctionservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing a notification sent to a user.
 */
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ID of the user to whom the notification is addressed.
     */
    @Column(nullable = false)
    private Long userId;

    /**
     * Notification message content.
     */
    @Column(nullable = false)
    private String message;

    /**
     * Type/category of the notification (e.g., OUTBID, NEW_BID, AUCTION_WON).
     */
    @Column(nullable = false)
    private String type;

    /**
     * Optional link for frontend navigation related to the notification.
     */
    private String link;

    /**
     * Indicates whether the notification has been read by the user.
     */
    @Column(nullable = false)
    private boolean read = false;

    /**
     * Timestamp when the notification was created.
     */
    @Column(nullable = false)
    private LocalDateTime timestamp;

    // Constructors

    public Notification() {
        this.timestamp = LocalDateTime.now();
    }

    public Notification(Long userId, String message, String type, String link) {
        this.userId = userId;
        this.message = message;
        this.type = type;
        this.link = link;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }

    public String getLink() {
        return link;
    }

    public boolean isRead() {
        return read;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
