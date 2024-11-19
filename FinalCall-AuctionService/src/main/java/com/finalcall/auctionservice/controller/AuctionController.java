/**
 * Acts as the main REST controller for handling HTTP requests related to auctions and bids.
 * Provides endpoints for creating, retrieving, updating, and deleting auctions and bids.
 * Handles auction bidding logic, including placing bids and buying items in Dutch auctions.
 * Integrates with WebSocket handlers to send real-time updates to clients.
 */

package com.finalcall.auctionservice.controller;

import feign.FeignException;
import org.springframework.web.bind.annotation.*;

import com.finalcall.auctionservice.database.AuctionRepository;
import com.finalcall.auctionservice.database.BidRepository;
import com.finalcall.auctionservice.dto.ItemDTO;
import com.finalcall.auctionservice.model.*;
import com.finalcall.auctionservice.model.Auction.AuctionStatus;
import com.finalcall.auctionservice.services.CatalogueServiceClient;
import com.finalcall.auctionservice.websocket.AuctionWSHandler;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

//**********************************************************************************************
// Swagger / OpenAPI Documentation available at: http://localhost:3200/swagger-ui/index.html#/
//**********************************************************************************************

@RestController
@RequestMapping("/auctions")
public class AuctionController {
    private final BidRepository bidRepository;
    private final AuctionRepository auctionRepository;
    private AuctionWSHandler auctionWSHandler;

    public AuctionController(BidRepository bidRepository, AuctionRepository auctionRepository, AuctionWSHandler auctionWSHandler) {
        this.bidRepository = bidRepository;
        this.auctionRepository = auctionRepository;
        this.auctionWSHandler = auctionWSHandler;
    }

    @GetMapping("/health")
    public ResponseEntity<?> getServiceHealth() {

        return new ResponseEntity<>("Service is running", HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<Auction>> getAllAuctions() {
        List<Auction> auctions = auctionRepository.findAll();

        return new ResponseEntity<>(auctions, HttpStatus.OK);
    }

    @GetMapping("/{auctionId}")
    public ResponseEntity<?> getAuctionDetails(@PathVariable Long auctionId) {
        Optional<Auction> auctionOptional = auctionRepository.findById(auctionId);

        if (auctionOptional.isPresent()) {
            Auction auction = auctionOptional.get();
            // Optionally, you might want to include additional details like item
            // information
            // or highest bid, depending on your application's requirements
            return new ResponseEntity<>(auction, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Auction not found", HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/items/{itemId}")
    public ResponseEntity<?> getAuctionByItemId(@PathVariable Long itemId) {
        Optional<Auction> auctionOptional = auctionRepository.findByItemId(itemId);

        if (auctionOptional.isPresent()) {
            return new ResponseEntity<>(auctionOptional.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Auction not found for Item ID: " + itemId, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/bids")
    public ResponseEntity<List<Bid>> getAllBids() {
        List<Bid> bids = bidRepository.findAll();

        return new ResponseEntity<>(bids, HttpStatus.OK);
    }

    @GetMapping("/bids/{bidId}")
    public ResponseEntity<?> getBidDetails(@PathVariable Long bidId) {
        Optional<Bid> bidOptional = bidRepository.findById(bidId);

        if (bidOptional.isPresent()) {
            return new ResponseEntity<>(bidOptional.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Bid not found", HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{auctionId}/bids")
    public ResponseEntity<List<Bid>> getAllBidsForAuction(@PathVariable Long auctionId) {
        List<Bid> bids = bidRepository.findByAuctionId(auctionId);

        return new ResponseEntity<>(bids, HttpStatus.OK);
    }

    @GetMapping("/{bidderId}/user-bids")
    public ResponseEntity<List<Bid>> getAllBidsForUser(@PathVariable Long bidderId) {
        List<Bid> bids = bidRepository.findAllByBidderId(bidderId);

        return new ResponseEntity<>(bids, HttpStatus.OK);
    }

    @PostMapping("/{auctionId}/bids")
    public ResponseEntity<?> placeForwardAuctionBid(@PathVariable Long auctionId, @RequestBody Bid bid) {
        try {
            // Check if the item exists using the CatalogueServiceClient
            // ItemDTO itemDTO = catalogueServiceClient.getItemById(itemId);

            // Check if the auction exists for this item
            Optional<Auction> auctionOptional = auctionRepository.findById(auctionId);
            Auction auction;

            if (auctionOptional.isPresent()) {
                // Use the existing auction
                auction = auctionOptional.get();
                // } else {
                // // Create a new auction as it does not exist
                // auction = new Auction();
                // auction.setItemId(itemId);
                // auction.addItemDetails(itemDTO);
                // auction.calculateStatus(); // Implement this method in Auction
                // auctionRepository.save(auction);
                // }

                // Check if the auction type is FORWARD
                if (auction.getType() != Auction.AuctionType.FORWARD || auction.getStatus() != Auction.AuctionStatus.ACTIVE) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Item not available for bidding.");
                }
                // Get the highest bid for this auction
                Bid highestBid = getHighestBidForAuction(auction.getId());

                // Check if the new bid is higher than the highest bid
                if (highestBid != null && bid.getAmount() <= highestBid.getAmount()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Bid must be higher than the current highest bid.");
                }

                // Save the new bid
                bid.setAuctionId(auction.getId());
                Bid savedBid = bidRepository.save(bid);

                // Update auction status if necessary
                auction.calculateStatus();
                auction.setCurrentBidPrice(savedBid.getAmount());
                auctionRepository.save(auction);
                try {
                    this.auctionWSHandler.broadcast(String.valueOf(auction.getId()), auction);
                } catch (Exception e) {
                    System.out.println("Unable to Send Updates");
                }

                return ResponseEntity.status(HttpStatus.CREATED).body(savedBid);
            } else {
                return new ResponseEntity<>("Auction not found", HttpStatus.NOT_FOUND);
            }
        } catch (FeignException.NotFound e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Item not found.");
        }
    }

    @PostMapping("/{auctionId}/buy-now")
    public ResponseEntity<?> placeDutchAuctionBid(@PathVariable Long auctionId, @RequestBody Bid bid) {

        Optional<Auction> optionalAuction = auctionRepository.findById(auctionId);

        if (!optionalAuction.isPresent()) {
            return new ResponseEntity<>("Auction not found.", HttpStatus.NOT_FOUND);
        }

        Auction auction = optionalAuction.get();
        // Check auction type and status
        if (auction.getType() != Auction.AuctionType.DUTCH || auction.getStatus() != Auction.AuctionStatus.ACTIVE) {
            return new ResponseEntity<>("Item not available for 'Buy Now'.", HttpStatus.BAD_REQUEST);
        }
        // Check if the auction type is FORWARD
        if (auction.getType() != Auction.AuctionType.DUTCH) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Only dutch auctions are eligible to buy now.");
        }
        // Verify buyNowBid amount
        if (bid.getAmount().compareTo(auction.getStartBidPrice().doubleValue()) != 0) {
            // getBuyNowPrice method
            return new ResponseEntity<>("Invalid bid amount for 'Buy Now'.", HttpStatus.BAD_REQUEST);
        }

        // Process the bid and auction update
        bid.setAuctionId(auction.getId());
        Bid savedBid = bidRepository.save(bid);
        auction.setCurrentBidPrice(bid.getAmount());
        auction.setStatus(Auction.AuctionStatus.SOLD); // Update auction status
        auctionRepository.save(auction);
        try {
            this.auctionWSHandler.broadcast(String.valueOf(auction.getId()), auction);
        } catch (Exception e) {
            System.out.println("Unable to Send Updates");
        }
        return new ResponseEntity<>(savedBid, HttpStatus.CREATED);

    }

    @GetMapping("/{auctionId}/bids/highest")
    public ResponseEntity<Bid> getHighestBidForItem(@PathVariable Long auctionId) {
        Optional<Auction> auction = auctionRepository.findById(auctionId);
        if (auction.isPresent()) {
            Bid highestBid = bidRepository.findByAuctionId(auction.get().getId()).stream().max(Comparator.comparing(Bid::getAmount)).orElse(null);
            if (highestBid != null) {
                return new ResponseEntity<>(highestBid, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{auctionId}/status")
    public ResponseEntity<?> getAuctionStatus(@PathVariable Long auctionId) {
        Optional<Auction> optionalAuction = auctionRepository.findById(auctionId);
        if (!optionalAuction.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Auction not found.");
        }

        Auction auction = optionalAuction.get();

        // You can directly return the auction object or create a custom DTO to format
        // the response
        // For example:
        return new ResponseEntity<>(auction, HttpStatus.OK);
    }

    @DeleteMapping("/bids/{bidId}")
    public ResponseEntity<?> deleteBid(@PathVariable Long bidId) {
        Optional<Bid> bidOptional = bidRepository.findById(bidId);
        if (bidOptional.isPresent()) {
            bidRepository.deleteById(bidId);
            return new ResponseEntity<>("Bid deleted successfully", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Bid not found", HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{auctionId}")
    public ResponseEntity<?> deleteAuction(@PathVariable Long auctionId) {
        Optional<Auction> auctionOptional = auctionRepository.findById(auctionId);

        if (auctionOptional.isPresent()) {
            // Before deleting the auction, you may want to perform some checks,
            // e.g., whether the auction can be deleted based on its status or other rules.

            auctionRepository.deleteById(auctionId);
            return new ResponseEntity<>("Auction deleted successfully", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Auction not found", HttpStatus.NOT_FOUND);
        }
    }

    private Bid getHighestBidForAuction(Long auctionId) {
        return bidRepository.findByAuctionId(auctionId).stream().max(Comparator.comparing(Bid::getAmount)).orElse(null);
    }

    @PostMapping("/{itemId}/new-auction")
    public ResponseEntity<?> createNewAuction(@PathVariable Long itemId, @RequestBody Auction newAuction) {
        // Check to see if this auction already exists for this item
        Optional<Auction> auctionOp = auctionRepository.findById(itemId);

        // if the auction already exists, then return a HTTP response saying it already
        // exists
        if (auctionOp.isPresent()) {
            return new ResponseEntity<>("Auction already exists", HttpStatus.ALREADY_REPORTED);
        }

        // if auction does not exist, add it to the database, and it will give it an
        // automatic auctionId
        // Set default values for any missing fields
        newAuction.setDefaultValues();

        auctionRepository.save(newAuction);
        try {
            this.auctionWSHandler.broadcast(String.valueOf(newAuction.getId()), newAuction);
        } catch (Exception e) {
            System.out.println("Unable to Send Updates");
        }
        return new ResponseEntity<>("Auction is now created", HttpStatus.OK);
    }

    @PutMapping("/{auctionId}")
    public ResponseEntity<?> updateAuction(@PathVariable Long auctionId, @RequestBody Auction updatedAuctionDetails) {
        Optional<Auction> existingAuction = auctionRepository.findById(auctionId);

        if (!existingAuction.isPresent()) {
            return new ResponseEntity<>("Auction not found", HttpStatus.NOT_FOUND);
        }

        Auction auction = existingAuction.get();

        auction.setItemId(updatedAuctionDetails.getItemId());
        auction.setCurrentBidPrice(updatedAuctionDetails.getCurrentBidPrice());
        auction.setStatus(updatedAuctionDetails.getStatus());

        auctionRepository.save(auction);
        try {
            this.auctionWSHandler.broadcast(String.valueOf(auction.getId()), auction);
        } catch (Exception e) {
            System.out.println("Unable to Send Updates");
        }
        return new ResponseEntity<>("Auction updated successfully", HttpStatus.OK);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Auction>> getAuctionsByStatus(@PathVariable Auction.AuctionStatus status) {
        List<Auction> auctions = auctionRepository.findByStatus(status);
        if (auctions.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(auctions, HttpStatus.OK);
    }

}