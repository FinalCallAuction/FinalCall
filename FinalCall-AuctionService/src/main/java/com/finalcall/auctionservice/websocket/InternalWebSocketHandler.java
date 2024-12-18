package com.finalcall.auctionservice.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finalcall.auctionservice.dto.AuctionDTO;
import com.finalcall.auctionservice.entity.Auction;
import com.finalcall.auctionservice.service.AuctionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class InternalWebSocketHandler implements WebSocketHandler {
    private final AuctionService auctionService;

    public InternalWebSocketHandler(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // Connection established
    }
    
    @Autowired
    private ObjectMapper objectMapper;


    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        Map<String, Object> request = objectMapper.readValue(message.getPayload().toString(), Map.class);
        String type = (String) request.get("type");
        String requestId = (String) request.get("requestId");
        Object data = request.get("data");

        Map<String, Object> response = new HashMap<>();
        response.put("requestId", requestId);

        try {
        	switch (type) {
            case "auction.getByItemId":
                Long itemId = Long.valueOf(data.toString());
                Optional<Auction> auction = auctionService.findByItemId(itemId);
                AuctionDTO auctionDTO = auction.map(a -> auctionService.mapToDTO(a)).orElse(null);
                response.put("data", auctionDTO);
                break;
            case "auction.create":
                AuctionDTO newAuction = objectMapper.convertValue(data, AuctionDTO.class);
                Auction createdAuction = auctionService.createAuction(newAuction);
                response.put("data", auctionService.mapToDTO(createdAuction));
                break;
            default:
                response.put("error", "Unknown request type: " + type);
        }

        } catch (Exception e) {
            response.put("error", e.getMessage());
        }

        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        // Handle transport error
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        // Connection closed
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}