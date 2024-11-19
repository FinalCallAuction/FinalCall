// src/main/java/com/finalcall/auctionservice/model/AuctionType.java

package com.finalcall.auctionservice.entity;

/**
 * Enumeration representing the types of auctions available.
 */
public enum AuctionType {
    FORWARD, // Traditional auction where bids increase over time
    DUTCH    // Auction where the price decreases until a bid is made
}
