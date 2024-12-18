package com.finalcall.auctionservice.controller;

import com.finalcall.auctionservice.dto.*;
import com.finalcall.auctionservice.entity.Auction;
import com.finalcall.auctionservice.entity.AuctionType;
import com.finalcall.auctionservice.exception.*;
import com.finalcall.auctionservice.service.AuctionService;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequestMapping("/api/auctions")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AuctionController {

    private static final Logger logger = LoggerFactory.getLogger(AuctionController.class);

    @Autowired
    private AuctionService auctionService;

    @PostMapping("/{auctionId}/bid")
    public ResponseEntity<?> placeBid(@PathVariable Long auctionId, @Valid @RequestBody BidRequest bidRequest,
                                    Authentication authentication) {
        try {
            logger.info("Received bid request for auction: {}", auctionId);
            logger.info("Bid request details: {}", bidRequest);
            
            if (authentication == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Authentication required to place bid");
            }

            Jwt jwt = (Jwt) authentication.getPrincipal();
            logger.info("JWT details - Subject: {}, Claims: {}", jwt.getSubject(), jwt.getClaims());
            
            logger.info("Processing bid request...");
            BidResponse response = auctionService.placeBid(auctionId, bidRequest);
            logger.info("Bid processed successfully");
            
            return ResponseEntity.ok(response);
        } catch (NoSuchElementException | IllegalStateException e) {
            logger.error("Bid error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error processing bid", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error processing bid: " + e.getMessage());
        }
    }
}