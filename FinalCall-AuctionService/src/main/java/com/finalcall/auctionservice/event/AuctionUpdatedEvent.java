// src/main/java/com/finalcall/auctionservice/event/AuctionUpdatedEvent.java

package com.finalcall.auctionservice.event;

import com.finalcall.auctionservice.entity.Auction;
import org.springframework.context.ApplicationEvent;

/**
 * Custom event for auction updates.
 */
public class AuctionUpdatedEvent extends ApplicationEvent {

    private final Auction auction;

    /**
     * Constructor for AuctionUpdatedEvent.
     *
     * @param source  The source of the event.
     * @param auction The updated Auction entity.
     */
    public AuctionUpdatedEvent(Object source, Auction auction) {
        super(source);
        this.auction = auction;
    }

    /**
     * Retrieves the updated Auction entity.
     *
     * @return The Auction entity.
     */
    public Auction getAuction() {
        return auction;
    }
}
