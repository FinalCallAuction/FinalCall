package com.finalcall.auctionservice.service;

import com.finalcall.auctionservice.dto.AuctionDTO;
import com.finalcall.auctionservice.dto.BidDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import com.finalcall.auctionservice.event.AuctionEvents.WebSocketEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WebSocketBroadcastService {
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    /**
     * Broadcast a new item listing to all clients subscribed to item updates.
     *
     * @param item The new ItemDTO to broadcast.
     */
    public void broadcastNewAuction(AuctionDTO auctionDTO) {
        eventPublisher.publishEvent(new WebSocketEvent(this, "NEW_AUCTION", auctionDTO, null, null));
    }

    /**
     * Broadcast an auction update to all clients subscribed to that auction.
     *
     * @param auctionId      The ID of the auction being updated.
     * @param auctionDTO     The updated auction details.
     * @param biddingHistory The current bidding history for the auction.
     */
    public void broadcastAuctionUpdate(Long auctionId, AuctionDTO auctionDTO, List<BidDTO> biddingHistory) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("auction", auctionDTO);
        payload.put("biddingHistory", biddingHistory);
        eventPublisher.publishEvent(new WebSocketEvent(this, "AUCTION_UPDATE", payload, null, auctionId));
    }

    /**
     * Send the initial auction state to a newly connected WebSocket session.
     *
     * @param session        The WebSocket session to send data to.
     * @param auctionId      The ID of the auction.
     * @param auctionDTO     The auction details.
     * @param biddingHistory The bidding history for the auction.
     */
    public void sendInitialAuctionState(WebSocketSession session, Long auctionId, AuctionDTO auctionDTO, List<BidDTO> biddingHistory) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("auction", auctionDTO);
        payload.put("biddingHistory", biddingHistory);
        eventPublisher.publishEvent(new WebSocketEvent(this, "AUCTION_UPDATE", payload, session, auctionId));
    }
}
