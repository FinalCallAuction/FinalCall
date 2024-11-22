// src/main/java/com/finalcall/auctionservice/controller/AuctionController.java

package com.finalcall.auctionservice.controller;

import com.finalcall.auctionservice.dto.AuctionDTO;
import com.finalcall.auctionservice.entity.Auction;
import com.finalcall.auctionservice.service.AuctionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auctions")
@CrossOrigin(origins = "*")
public class AuctionController {

    @Autowired
    private AuctionService auctionService;

    /**
     * Create a new auction.
     *
     * @param auctionDTO Details of the auction.
     * @return ResponseEntity with the created auction or an error message.
     */
    @PostMapping
    public ResponseEntity<?> createAuction(@RequestBody AuctionDTO auctionDTO) {
        try {
            Auction auction = auctionService.createAuction(auctionDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(auction);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating auction.");
        }
    }

    /**
     * Get auction details by item ID.
     *
     * @param itemId ID of the item.
     * @return ResponseEntity containing AuctionDTO or error message.
     */
    @GetMapping("/item/{itemId}")
    public ResponseEntity<?> getAuctionByItemId(@PathVariable Long itemId) {
        Optional<Auction> auctionOpt = auctionService.findByItemId(itemId);
        if (auctionOpt.isPresent()) {
            Auction auction = auctionOpt.get();
            AuctionDTO auctionDTO = new AuctionDTO(
                auction.getItemId(),
                auction.getAuctionType(),
                auction.getStartingBidPrice(),
                auction.getCurrentBidPrice(),
                auction.getAuctionEndTime(),
                auction.getSellerId(),
                auction.getStartTime()
            );
            return ResponseEntity.ok(auctionDTO);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Auction not found for the given item ID.");
        }
    }

    /**
     * Place a new bid on an auction.
     *
     * @param auctionId The ID of the auction.
     * @param bidPrice  The new bid price.
     * @return ResponseEntity with the updated auction or an error message.
     */
    @PostMapping("/bid/{auctionId}")
    public ResponseEntity<?> placeBid(@PathVariable Long auctionId, @RequestParam Double bidPrice) {
        Optional<Auction> updatedAuctionOpt = auctionService.updateCurrentBidPrice(auctionId, bidPrice);
        if (updatedAuctionOpt.isPresent()) {
            return ResponseEntity.ok(updatedAuctionOpt.get());
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Bid price is too low or auction not found.");
        }
    }
}
