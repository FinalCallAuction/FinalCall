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
    private final ObjectMapper objectMapper;

    @Autowired
    public InternalWebSocketHandler(AuctionService auctionService, ObjectMapper objectMapper) {
        this.auctionService = auctionService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // Connection established
    }

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
                case "auction.create":
                    AuctionDTO newAuction = objectMapper.convertValue(data, AuctionDTO.class);
                    Auction created = auctionService.createAuction(newAuction);
                    response.put("data", auctionService.mapToDTO(created));
                    break;

                case "auction.getByItemId":
                    Long itemId = Long.valueOf(data.toString());
                    Optional<Auction> optAuc = auctionService.findByItemId(itemId);
                    AuctionDTO aucDTO = optAuc.map(a -> auctionService.mapToDTO(a)).orElse(null);
                    response.put("data", aucDTO);
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
        exception.printStackTrace();
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
