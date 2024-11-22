package com.finalcall.auctionservice.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finalcall.auctionservice.dto.AuctionDTO;
import com.finalcall.auctionservice.entity.Auction;
import com.finalcall.auctionservice.entity.Bid;
import com.finalcall.auctionservice.event.AuctionUpdatedEvent;
import com.finalcall.auctionservice.service.AuctionService;
import com.finalcall.auctionservice.repository.BidRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket handler for auction updates.
 */
@Component
public class AuctionWSHandler extends TextWebSocketHandler {

    private final BidRepository bidRepository;
    private final AuctionService auctionService;
    private final ObjectMapper objectMapper;
    
    // Thread-safe map to hold auctionId to sessions
    private static Map<String, Set<WebSocketSession>> auctionSessions = new ConcurrentHashMap<>();

    public AuctionWSHandler(BidRepository bidRepository,
                            AuctionService auctionService,
                            ObjectMapper objectMapper) {
        this.bidRepository = bidRepository;
        this.auctionService = auctionService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String auctionId = getAuctionId(session);
        if (auctionId.equals("0")) { // Invalid auctionId
            session.close(CloseStatus.BAD_DATA);
            return;
        }
        auctionSessions.computeIfAbsent(auctionId, k -> ConcurrentHashMap.newKeySet()).add(session);
        // Optionally send initial data to the client
        sendAuctionUpdate(session, Long.parseLong(auctionId));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String auctionId = getAuctionId(session);
        if (auctionSessions.containsKey(auctionId)) {
            Set<WebSocketSession> sessions = auctionSessions.get(auctionId);
            sessions.remove(session);
            if (sessions.isEmpty()) {
                auctionSessions.remove(auctionId);
            }
        }
    }

    /**
     * Sends the current auction state to the specified WebSocket session.
     *
     * @param session   The WebSocket session.
     * @param auctionId The ID of the auction.
     * @throws IOException If sending the message fails.
     */
    private void sendAuctionUpdate(WebSocketSession session, Long auctionId) throws IOException {
        try {
            // Fetch the Auction entity
            Optional<Auction> auctionOpt = auctionService.findByItemId(auctionId);
            if (auctionOpt.isPresent()) {
                Auction auction = auctionOpt.get();
                // Map to AuctionDTO
                AuctionDTO auctionDTO = auctionService.mapToDTO(auction);
                // Fetch bidding history
                List<Bid> bids = auctionService.getBidsForAuction(auctionId);
                // Find highest bid
                Bid highestBid = bids.stream().max(Comparator.comparing(Bid::getAmount)).orElse(null);
                // Prepare response
                Map<String, Object> response = new HashMap<>();
                response.put("auction", auctionDTO);
                response.put("highestBid", highestBid);

                String responseStr = objectMapper.writeValueAsString(response);
                session.sendMessage(new TextMessage(responseStr));
            } else {
                // Auction not found, send error
                Map<String, String> error = new HashMap<>();
                error.put("error", "Auction not found");
                String errorStr = objectMapper.writeValueAsString(error);
                session.sendMessage(new TextMessage(errorStr));
            }
        } catch (Exception e) {
            // Log the exception
            e.printStackTrace();
            // Send error message to client
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error fetching auction data");
            String errorStr = objectMapper.writeValueAsString(error);
            session.sendMessage(new TextMessage(errorStr));
        }
    }

    /**
     * Broadcasts the updated auction data to all connected clients for the specified auction.
     *
     * @param auctionId The ID of the auction.
     * @param auctionDTO The updated auction data.
     * @throws IOException If sending the message fails.
     */
    public void broadcast(String auctionId, AuctionDTO auctionDTO) throws IOException {
        Set<WebSocketSession> sessions = auctionSessions.get(auctionId);
        if (sessions != null && !sessions.isEmpty()) {
            // Fetch the latest bidding history
            List<Bid> bids = auctionService.getBidsForAuction(Long.parseLong(auctionId));
            Bid highestBid = bids.stream().max(Comparator.comparing(Bid::getAmount)).orElse(null);

            Map<String, Object> response = new HashMap<>();
            response.put("auction", auctionDTO);
            response.put("highestBid", highestBid);

            String responseStr = objectMapper.writeValueAsString(response);
            TextMessage message = new TextMessage(responseStr);

            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    session.sendMessage(message);
                }
            }
        }
    }

    /**
     * Extracts the auction ID from the WebSocket session's URI.
     *
     * @param session The WebSocket session.
     * @return The auction ID as a string, or "0" if not found.
     */
    private String getAuctionId(WebSocketSession session) {
        // Assuming the URI pattern is /auctions/{auctionId}/update
        String path = session.getUri().getPath();
        String[] segments = path.split("/");
        return (segments.length >= 3) ? segments[2] : "0"; // Adjust index as needed
    }
}
