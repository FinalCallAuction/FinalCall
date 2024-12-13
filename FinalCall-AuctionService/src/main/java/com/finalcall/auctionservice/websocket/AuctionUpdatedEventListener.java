// src/main/java/com/finalcall/auctionservice/websocket/AuctionUpdatedEventListener.java

package com.finalcall.auctionservice.websocket;

import com.finalcall.auctionservice.dto.AuctionDTO;
import com.finalcall.auctionservice.entity.Auction;
import com.finalcall.auctionservice.event.AuctionUpdatedEvent;
import com.finalcall.auctionservice.service.AuctionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AuctionUpdatedEventListener {

    @Autowired
    private AuctionWSHandler auctionWSHandler;

    @Autowired
    private AuctionService auctionService;

    /**
     * Listens for AuctionUpdatedEvent and broadcasts updates via WebSocket.
     *
     * @param event The AuctionUpdatedEvent.
     */
    @EventListener
    public void handleAuctionUpdatedEvent(AuctionUpdatedEvent event) {
        Auction auction = event.getAuction();
        AuctionDTO auctionDTO = auctionService.mapToDTO(auction);
        Long auctionId = auction.getId();
        auctionWSHandler.broadcastAuctionUpdate(auctionId, auctionDTO);
    }
}
