// src/main/java/com/finalcall/auctionservice/websocket/AuctionWSHandler.java

package com.finalcall.auctionservice.websocket;

import com.finalcall.auctionservice.dto.AuctionDTO;
import com.finalcall.auctionservice.service.AuctionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AuctionWSHandler extends TextWebSocketHandler {

    @Autowired
    private AuctionService auctionService;

    @Autowired
    private ObjectMapper objectMapper;

    // Mapping from auctionId to a set of WebSocket sessions
    private static Map<Long, Set<WebSocketSession>> auctionSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long auctionId = extractAuctionId(session);
        if (auctionId == null || auctionId == 0L) {
            session.close(CloseStatus.BAD_DATA);
            return;
        }
        auctionSessions.computeIfAbsent(auctionId, k -> ConcurrentHashMap.newKeySet()).add(session);
        // Optionally, send current auction state
        sendAuctionUpdate(session, auctionId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long auctionId = extractAuctionId(session);
        if (auctionId != null && auctionId != 0L) {
            Set<WebSocketSession> sessions = auctionSessions.get(auctionId);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    auctionSessions.remove(auctionId);
                }
            }
        }
    }

    /**
     * Sends the current auction state to the specified session.
     *
     * @param session   The WebSocket session.
     * @param auctionId The ID of the auction.
     * @throws IOException If sending the message fails.
     */
    private void sendAuctionUpdate(WebSocketSession session, Long auctionId) throws IOException {
        Optional<AuctionDTO> auctionDTOOpt = auctionService.findByItemId(auctionId)
                .map(auctionService::mapToDTO);

        if (auctionDTOOpt.isPresent()) {
            AuctionDTO auctionDTO = auctionDTOOpt.get();
            Map<String, Object> response = new HashMap<>();
            response.put("auction", auctionDTO);
            response.put("biddingHistory", auctionService.getBidsForAuction(auctionId));

            String message = objectMapper.writeValueAsString(response);
            session.sendMessage(new TextMessage(message));
        } else {
            session.sendMessage(new TextMessage("{\"error\":\"Auction not found.\"}"));
        }
    }

    /**
     * Broadcasts auction updates to all connected clients for a specific auction.
     *
     * @param auctionId  The ID of the auction.
     * @param auctionDTO The updated auction data.
     */
    public void broadcastAuctionUpdate(Long auctionId, AuctionDTO auctionDTO) {
        Set<WebSocketSession> sessions = auctionSessions.get(auctionId);
        if (sessions != null && !sessions.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("auction", auctionDTO);
            response.put("biddingHistory", auctionService.getBidsForAuction(auctionId));

            try {
                String message = objectMapper.writeValueAsString(response);
                TextMessage textMessage = new TextMessage(message);
                for (WebSocketSession session : sessions) {
                    if (session.isOpen()) {
                        session.sendMessage(textMessage);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Extracts the auction ID from the WebSocket session's URL.
     *
     * @param session The WebSocket session.
     * @return The auction ID as a Long, or 0L if not found.
     */
    private Long extractAuctionId(WebSocketSession session) {
        try {
            String path = session.getUri().getPath(); // e.g., /ws/auctions/{auctionId}
            String[] segments = path.split("/");
            if (segments.length >= 4) {
                return Long.parseLong(segments[3]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0L;
    }
}
