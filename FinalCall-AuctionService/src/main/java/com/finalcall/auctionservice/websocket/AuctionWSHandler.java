package com.finalcall.auctionservice.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finalcall.auctionservice.dto.AuctionDTO;
import com.finalcall.auctionservice.entity.Auction;
import com.finalcall.auctionservice.entity.Bid;
import com.finalcall.auctionservice.repository.BidRepository;
import com.finalcall.auctionservice.service.AuctionService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;

@Component
public class AuctionWSHandler extends TextWebSocketHandler {

    private final BidRepository bidRepository;
    private final AuctionService auctionService;
    private final ObjectMapper objectMapper;
    private static Map<String, Set<WebSocketSession>> auctionSessions = new HashMap<>();

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
        if (!auctionSessions.containsKey(auctionId)) {
            auctionSessions.put(auctionId, new HashSet<>());
        }
        auctionSessions.get(auctionId).add(session);
        // Optionally send initial data to the client
        sendAuctionUpdate(session, Long.parseLong(auctionId));
    }

    private void sendAuctionUpdate(WebSocketSession session, Long auctionId) throws IOException {
        try {
            // Fetch Auction entity by item ID
            Optional<Auction> auctionOpt = auctionService.findByItemId(auctionId);
            if (auctionOpt.isPresent()) {
                Auction auction = auctionOpt.get();
                
                // Map Auction to AuctionDTO
                AuctionDTO auctionDTO = mapToDTO(auction);
                
                List<Bid> bids = bidRepository.findByAuctionId(auctionId);
                
                // Create a response with auction details and highest bid
                String response = createAuctionResponse(auctionDTO, bids);
                session.sendMessage(new TextMessage(response));
            } else {
                session.sendMessage(new TextMessage("{\"error\": \"Auction not found\"}"));
            }
        } catch (Exception e) {
            session.sendMessage(new TextMessage("{\"error\": \"Error fetching auction data\"}"));
            // Optionally log the error
            e.printStackTrace();
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String auctionId = getAuctionId(session);
        Set<WebSocketSession> sessions = auctionSessions.get(auctionId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                auctionSessions.remove(auctionId);
            }
        }
    }

    private String createAuctionResponse(AuctionDTO auction, List<Bid> bids) {
        try {
            // Create a Map to hold the response data
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("auction", auction);
            responseData.put("highestBid", bids.stream().max(Comparator.comparing(Bid::getAmount)).orElse(null));

            return objectMapper.writeValueAsString(responseData);
        } catch (IOException e) {
            // Handle exceptions, possibly logging them and returning an error message
            return "{\"error\": \"Error creating auction response\"}";
        }
    }

    /**
     * Broadcast updated auction details to all connected clients for a specific auction.
     *
     * @param auctionId ID of the auction.
     * @param auction   Updated auction details.
     * @throws IOException If sending messages fails.
     */
    public void broadcast(String auctionId, AuctionDTO auction) throws IOException {
        Set<WebSocketSession> sessions = auctionSessions.get(auctionId);
        if (sessions != null) {
            for (WebSocketSession session : sessions) {
                sendAuctionUpdate(session, Long.parseLong(auctionId));
            }
        }
    }

    /**
     * Extracts the auction ID from the WebSocket session's URI.
     *
     * @param session WebSocketSession.
     * @return Auction ID as a string.
     */
    private String getAuctionId(WebSocketSession session) {
        // Assuming the URI pattern is /auctions/{auctionId}/update
        String path = session.getUri().getPath();
        String[] segments = path.split("/");
        return segments.length >= 3 ? segments[2] : "0"; // Adjust index as needed
    }

    /**
     * Maps an Auction entity to AuctionDTO.
     *
     * @param auction The Auction entity.
     * @return AuctionDTO object.
     */
    private AuctionDTO mapToDTO(Auction auction) {
        AuctionDTO dto = new AuctionDTO();
        dto.setItemId(auction.getItemId());
        dto.setAuctionType(auction.getAuctionType());
        dto.setStartingBidPrice(auction.getStartingBidPrice());
        dto.setCurrentBidPrice(auction.getCurrentBidPrice());
        dto.setAuctionEndTime(auction.getAuctionEndTime());
        dto.setSellerId(auction.getSellerId());
        dto.setStartTime(auction.getStartTime());
        return dto;
    }
}
