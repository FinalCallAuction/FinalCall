// src/main/java/com/finalcall/auctionservice/entity/AuctionUpdatedEvent.java

package com.finalcall.auctionservice.entity;

import org.springframework.context.ApplicationEvent;

public class AuctionUpdatedEvent extends ApplicationEvent {

    private final Auction auction;

    public AuctionUpdatedEvent(Object source, Auction auction) {
        super(source);
        this.auction = auction;
    }

    public Auction getAuction() {
        return auction;
    }
}
