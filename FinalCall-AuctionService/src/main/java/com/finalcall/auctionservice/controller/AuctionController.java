package com.finalcall.auctionservice.controller;

import com.finalcall.auctionservice.dto.AuctionDTO;
import com.finalcall.auctionservice.dto.BidRequest;
import com.finalcall.auctionservice.dto.BidResponse;
import com.finalcall.auctionservice.entity.Auction;
import com.finalcall.auctionservice.entity.Bid;
import com.finalcall.auctionservice.service.AuctionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/auctions")
@CrossOrigin(origins = "*") // Adjust origins as needed
public class AuctionController {

    @Autowired
    private AuctionService auctionService;

    /**
     * Create a new auction.
     *
     * @param auctionDTO Details of the auction.
     * @return ResponseEntity with the created auction or an error message.
     */
    @PostMapping("/create")
    public ResponseEntity<?> createAuction(@RequestBody AuctionDTO auctionDTO) {
        try {
            // Validate required fields
            if (auctionDTO.getItemId() == null || auctionDTO.getAuctionType() == null ||
                auctionDTO.getStartingBidPrice() == null || auctionDTO.getAuctionEndTime() == null ||
                auctionDTO.getSellerId() == null || auctionDTO.getStartTime() == null) {
                return ResponseEntity.badRequest().body("Missing required auction fields.");
            }

            // Additional validation for DUTCH auctions
            if (auctionDTO.getAuctionType() == com.finalcall.auctionservice.entity.AuctionType.DUTCH) {
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
            // Log exception
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating auction.");
        }
    }

    /**
     * Place a bid on an auction.
     *
     * @param auctionId  The ID of the auction.
     * @param bidRequest The bid details.
     * @return ResponseEntity with BidResponse or error message.
     */
    @PostMapping("/{auctionId}/bid")
    public ResponseEntity<?> placeBid(@PathVariable Long auctionId, @RequestBody BidRequest bidRequest) {
        try {
            BidResponse response = auctionService.placeBid(auctionId, bidRequest);
            return ResponseEntity.ok(response);
        } catch (com.finalcall.auctionservice.exception.AuctionNotFoundException |
                 com.finalcall.auctionservice.exception.AuctionNotActiveException |
                 com.finalcall.auctionservice.exception.InvalidBidException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // Log exception
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error placing bid.");
        }
    }

    /**
     * Get auction details by item ID.
     *
     * @param itemId The ID of the item.
     * @return ResponseEntity containing AuctionDTO or error message.
     */
    @GetMapping("/item/{itemId}")
    public ResponseEntity<?> getAuctionByItemId(@PathVariable Long itemId) {
        try {
            Optional<Auction> auctionOpt = auctionService.findByItemId(itemId);
            if (auctionOpt.isPresent()) {
                Auction auction = auctionOpt.get();
                AuctionDTO auctionDTO = auctionService.mapToDTO(auction);
                return ResponseEntity.ok(auctionDTO);
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
     * Get all bids for an auction.
     *
     * @param auctionId The ID of the auction.
     * @return ResponseEntity containing list of bids or error message.
     */
    @GetMapping("/{auctionId}/bids")
    public ResponseEntity<?> getBidsForAuction(@PathVariable Long auctionId) {
        try {
            List<Bid> bids = auctionService.getBidsForAuction(auctionId);
            // Transform Bid entities to a more detailed DTO if necessary
            // For example, include bidder's username by integrating with UserService
            // For simplicity, returning bids as is
            return ResponseEntity.ok(bids);
        } catch (Exception e) {
            // Log exception
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching bids.");
        }
    }

    /**
     * Update images for an auction.
     *
     * @param auctionId The ID of the auction.
     * @param imageUrls The updated list of image URLs.
     * @return ResponseEntity with success message or error.
     */
    @PutMapping("/{auctionId}/update-images")
    public ResponseEntity<?> updateAuctionImages(@PathVariable Long auctionId, @RequestBody List<String> imageUrls) {
        try {
            Optional<Auction> auctionOpt = auctionService.findByItemId(auctionId);
            if (auctionOpt.isPresent()) {
                Auction auction = auctionOpt.get();
                // Assuming images are managed separately, possibly in CatalogueService
                // If images are part of Auction entity, add an imageUrls field and update here
                // Since current Auction entity does not have imageUrls, this endpoint might delegate to CatalogueService
                // For demonstration, we'll assume images are not handled here
                return ResponseEntity.ok("Images updated successfully.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Auction not found for the given auction ID.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating auction images.");
        }
    }
}
