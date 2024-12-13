// src/main/java/com/finalcall/auctionservice/controller/AuctionController.java

package com.finalcall.auctionservice.controller;

import com.finalcall.auctionservice.dto.*;
import com.finalcall.auctionservice.entity.Auction;
import com.finalcall.auctionservice.entity.AuctionType;
import com.finalcall.auctionservice.service.AuctionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequestMapping("/api/auctions")
@CrossOrigin(origins = "http://localhost:3000") // Restrict to frontend origin
public class AuctionController {

    @Autowired
    private AuctionService auctionService;

    /**
     * Creates a new auction.
     *
     * @param auctionDTO Details of the auction.
     * @return ResponseEntity with the created auction or an error message.
     */
    @PostMapping("/create")
    public ResponseEntity<?> createAuction(@Valid @RequestBody AuctionDTO auctionDTO) {
        try {

            // Basic validation is handled by @Valid and DTO constraints

            // Additional validation for DUTCH auctions
            if (auctionDTO.getAuctionType() == AuctionType.DUTCH) {
                if (auctionDTO.getPriceDecrement() == null || auctionDTO.getMinimumPrice() == null) {
                    return ResponseEntity.badRequest().body("Price Decrement and Minimum Price must be provided for DUTCH auctions.");
                }
                if (auctionDTO.getPriceDecrement() <= 0 || auctionDTO.getMinimumPrice() <= 0) {
                    return ResponseEntity.badRequest().body("Price Decrement and Minimum Price must be positive values.");
                }
                if (auctionDTO.getMinimumPrice() >= auctionDTO.getStartingBidPrice()) {
                    return ResponseEntity.badRequest().body("Minimum Price must be less than Starting Bid Price for DUTCH auctions.");
                }
            }

            Auction createdAuction = auctionService.createAuction(auctionDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdAuction);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating auction.");
        }
    }

    /**
     * Places a bid on an auction.
     *
     * @param auctionId  The ID of the auction.
     * @param bidRequest The bid details.
     * @return ResponseEntity with BidResponse or error message.
     */
    @PostMapping("/{auctionId}/bid")
    public ResponseEntity<?> placeBid(@PathVariable Long auctionId, @Valid @RequestBody BidRequest bidRequest) {
        try {
            BidResponse response = auctionService.placeBid(auctionId, bidRequest);
            return ResponseEntity.ok(response);
        } catch (NoSuchElementException | IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // Log exception
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error placing bid.");
        }
    }

    /**
     * Retrieves auction details by item ID.
     *
     * @param itemId The ID of the item.
     * @return ResponseEntity containing AuctionDTO or error message.
     */
    @GetMapping("/item/{itemId}")
    public ResponseEntity<?> getAuctionByItemId(@PathVariable Long itemId) {
        try {
            Optional<Auction> optionalAuction = auctionService.findByItemId(itemId);
            if (optionalAuction.isPresent()) {
                AuctionDTO dto = auctionService.mapToDTO(optionalAuction.get());
                return ResponseEntity.ok(dto);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Auction not found for the given item ID.");
            }
        } catch (Exception e) {
            // Log exception
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching auction details.");
        }
    }

    /**
     * Retrieves all bids for a specific auction.
     *
     * @param auctionId The ID of the auction.
     * @return ResponseEntity containing list of BidDTOs or error message.
     */
    @GetMapping("/{auctionId}/bids")
    public ResponseEntity<?> getBidsForAuction(@PathVariable Long auctionId) {
        try {
            List<BidDTO> bids = auctionService.getBidsForAuction(auctionId);
            return ResponseEntity.ok(bids);
        } catch (Exception e) {
            // Log exception
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching bids.");
        }
    }
}
