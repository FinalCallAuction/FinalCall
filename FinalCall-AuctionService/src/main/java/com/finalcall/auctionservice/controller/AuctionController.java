package com.finalcall.auctionservice.controller;

import com.finalcall.auctionservice.dto.*;
import com.finalcall.auctionservice.entity.Auction;
import com.finalcall.auctionservice.entity.AuctionType;
import com.finalcall.auctionservice.exception.*;
import com.finalcall.auctionservice.service.AuctionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequestMapping("/api/auctions")
@CrossOrigin(origins = "http://localhost:3000")
public class AuctionController {

    @Autowired
    private AuctionService auctionService;

    /**
     * Creates a new auction.
     *
     * @param auctionDTO Details of the auction.
     * @return ResponseEntity with the created AuctionDTO or an error message.
     */
    @PostMapping("/create")
    public ResponseEntity<?> createAuction(@Valid @RequestBody AuctionDTO auctionDTO) {
        try {
            if (auctionDTO.getAuctionType() == AuctionType.DUTCH) {
                if (auctionDTO.getMinimumPrice() == null || auctionDTO.getMinimumPrice() <= 0) {
                    return ResponseEntity.badRequest().body("Minimum Price must be a positive value for DUTCH auctions.");
                }
                if (auctionDTO.getMinimumPrice() >= auctionDTO.getStartingBidPrice()) {
                    return ResponseEntity.badRequest().body("Minimum Price must be less than Starting Bid Price for Dutch auctions.");
                }
            }
            Auction createdAuction = auctionService.createAuction(auctionDTO);
            AuctionDTO responseDTO = auctionService.mapToDTO(createdAuction);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating auction.");
        }
    }


    @PostMapping("/{auctionId}/bid")
    public ResponseEntity<?> placeBid(@PathVariable Long auctionId, @Valid @RequestBody BidRequest bidRequest) {
        try {
            BidResponse response = auctionService.placeBid(auctionId, bidRequest);
            return ResponseEntity.ok(response);
        } catch (NoSuchElementException | IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error placing bid.");
        }
    }

    @GetMapping("/item/{itemId}")
    public ResponseEntity<?> getAuctionByItemId(@PathVariable Long itemId) {
        try {
            Optional<Auction> optionalAuction = auctionService.findByItemId(itemId);
            if (optionalAuction.isPresent()) {
                AuctionDTO dto = auctionService.mapToDTO(optionalAuction.get());
                return ResponseEntity.ok(dto);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Auction not found.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching auction details.");
        }
    }

    @GetMapping("/{auctionId}/bids")
    public ResponseEntity<?> getBidsForAuction(@PathVariable Long auctionId) {
        try {
            List<BidDTO> bids = auctionService.getBidsForAuction(auctionId);
            return ResponseEntity.ok(bids);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching bids.");
        }
    }

    @GetMapping("/user/{userId}/bids")
    public ResponseEntity<?> getUserBids(@PathVariable Long userId) {
        try {
            List<BidDTO> userBids = auctionService.getUserBids(userId);
            return ResponseEntity.ok(userBids);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching user bids");
        }
    }

    @PostMapping("/{auctionId}/decrement")
    public ResponseEntity<?> manualDecrement(@PathVariable Long auctionId, @RequestBody DecrementRequest request) {
        try {
            BidResponse response = auctionService.manualDecrement(auctionId, request.getUserId(), request.getDecrementAmount());
            return ResponseEntity.ok(response);
        } catch (AuctionNotFoundException | InvalidDecrementException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error performing price decrement.");
        }
    }
}
