// src/main/java/com/finalcall/auctionservice/controller/AuctionController.java

package com.finalcall.auctionservice.controller;

import com.finalcall.auctionservice.dto.AuctionDTO;
import com.finalcall.auctionservice.dto.ItemDTO;
import com.finalcall.auctionservice.entity.Auction;
import com.finalcall.auctionservice.entity.AuctionStatus;
import com.finalcall.auctionservice.entity.AuctionType;
import com.finalcall.auctionservice.entity.Bid;
import com.finalcall.auctionservice.repository.AuctionRepository;
import com.finalcall.auctionservice.repository.BidRepository;
import com.finalcall.auctionservice.services.AuctionService;
import com.finalcall.auctionservice.services.CatalogueServiceClient;
import com.finalcall.auctionservice.websocket.AuctionWSHandler;

import feign.FeignException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
/**
 * REST Controller for managing Auctions.
 */
@RestController
@RequestMapping("/api/auctions")
@CrossOrigin(origins = "*")
public class AuctionController {
    private final BidRepository bidRepository;
    private final AuctionRepository auctionRepository;
    private final AuctionWSHandler auctionWSHandler;
    private final CatalogueServiceClient catalogueServiceClient;

    @Autowired
    private AuctionService auctionService;
    
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    public AuctionController(BidRepository bidRepository, AuctionRepository auctionRepository, 
                             AuctionWSHandler auctionWSHandler, CatalogueServiceClient catalogueServiceClient) {
        this.bidRepository = bidRepository;
        this.auctionRepository = auctionRepository;
        this.auctionWSHandler = auctionWSHandler;
        this.catalogueServiceClient = catalogueServiceClient;
    }
    
    /**
     * Creates a new auction by delegating to the AuctionService.
     *
     * @param auctionDTO The auction details.
     * @return ResponseEntity with the created AuctionDTO or an error message.
     */
    @PostMapping("/create")
    public ResponseEntity<?> createAuction(@RequestBody AuctionDTO auctionDTO, @AuthenticationPrincipal Jwt principal) {
        try {
            AuctionDTO createdAuction = auctionService.createAuction(auctionDTO, principal);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdAuction);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid auction data: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating auction: " + e.getMessage());
        }
    }

    /**
     * Health check endpoint.
     *
     * @return A simple message indicating the service is running.
     */
    @GetMapping("/health")
    public ResponseEntity<?> getServiceHealth() {
        return new ResponseEntity<>("Auction Service is running", HttpStatus.OK);
    }

    /**
     * Retrieves all auctions.
     *
     * @return List of all Auction entities.
     */
    @GetMapping
    public ResponseEntity<List<Auction>> getAllAuctions() {
        List<Auction> auctions = auctionRepository.findAll();
        return new ResponseEntity<>(auctions, HttpStatus.OK);
    }

    /**
     * Retrieves auction details by auction ID.
     *
     * @param auctionId ID of the auction.
     * @return Auction details or an error message.
     */
    @GetMapping("/{auctionId}")
    public ResponseEntity<?> getAuctionDetails(@PathVariable Long auctionId) {
        Optional<Auction> auctionOptional = auctionRepository.findById(auctionId);
        if (auctionOptional.isPresent()) {
            Auction auction = auctionOptional.get();
            return new ResponseEntity<>(auction, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Auction not found", HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Retrieves auction details by catalogue item ID.
     *
     * @param itemId ID of the catalogue item.
     * @return Auction details or an error message.
     */
    @GetMapping("/items/{itemId}")
    public ResponseEntity<?> getAuctionByItemId(@PathVariable Long itemId) {
        Optional<Auction> auctionOptional = auctionRepository.findByCatalogueItemId(itemId);
        if (auctionOptional.isPresent()) {
            return new ResponseEntity<>(auctionOptional.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Auction not found for Item ID: " + itemId, HttpStatus.NOT_FOUND);
        }
    }
    
    /**
     * Retrieves auctions by a list of catalogue item IDs.
     *
     * @param itemIds List of catalogue item IDs.
     * @return List of corresponding AuctionDTOs or an error message.
     */
    @GetMapping("/by-item-ids")
    public ResponseEntity<?> getAuctionsByCatalogueItemIds(@RequestParam("itemIds") List<Long> itemIds) {
        try {
            List<AuctionDTO> auctions = auctionService.getAuctionsByCatalogueItemIds(itemIds);
            return ResponseEntity.ok(auctions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching auctions: " + e.getMessage());
        }
    }

    /**
     * Retrieves all bids.
     *
     * @return List of all Bid entities.
     */
    @GetMapping("/bids")
    public ResponseEntity<List<Bid>> getAllBids() {
        List<Bid> bids = bidRepository.findAll();
        return new ResponseEntity<>(bids, HttpStatus.OK);
    }

    /**
     * Retrieves bid details by bid ID.
     *
     * @param bidId ID of the bid.
     * @return Bid details or an error message.
     */
    @GetMapping("/bids/{bidId}")
    public ResponseEntity<?> getBidDetails(@PathVariable Long bidId) {
        Optional<Bid> bidOptional = bidRepository.findById(bidId);
        if (bidOptional.isPresent()) {
            return new ResponseEntity<>(bidOptional.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Bid not found", HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Retrieves all bids for a specific auction.
     *
     * @param auctionId ID of the auction.
     * @return List of corresponding bids.
     */
    @GetMapping("/{auctionId}/bids")
    public ResponseEntity<List<Bid>> getAllBidsForAuction(@PathVariable Long auctionId) {
        List<Bid> bids = bidRepository.findByAuctionId(auctionId);
        return new ResponseEntity<>(bids, HttpStatus.OK);
    }

    /**
     * Retrieves all bids placed by a specific user.
     *
     * @param bidderId ID of the bidder.
     * @return List of corresponding bids.
     */
    @GetMapping("/bids/user/{bidderId}")
    public ResponseEntity<List<Bid>> getAllBidsForUser(@PathVariable Long bidderId) {
        List<Bid> bids = bidRepository.findAllByBidderId(bidderId);
        return new ResponseEntity<>(bids, HttpStatus.OK);
    }

    /**
     * Creates a new auction for an existing item.
     *
     * @param itemId     ID of the item to auction.
     * @param newAuction Auction details.
     * @return ResponseEntity indicating success or failure.
     */
    @PostMapping("/{itemId}/new-auction")
    public ResponseEntity<?> createNewAuction(@PathVariable Long itemId, @RequestBody Auction newAuction) {
        Optional<Auction> auctionOp = auctionRepository.findByCatalogueItemId(itemId);
        if (auctionOp.isPresent()) {
            return new ResponseEntity<>("Auction already exists for this item", HttpStatus.CONFLICT);
        }

        try {
            ItemDTO itemDTO = catalogueServiceClient.getItemById(itemId);
            if (itemDTO == null) {
                return new ResponseEntity<>("Item not found.", HttpStatus.NOT_FOUND);
            }

            // Set auction details
            newAuction.setCatalogueItemId(itemId);
            newAuction.setSellerId(itemDTO.getListedBy()); // Correctly retrieves listedBy
            newAuction.setStatus(AuctionStatus.ACTIVE);
            newAuction.setStartTime(LocalDateTime.now());
            newAuction.setAuctionEndTime(newAuction.getAuctionEndTime() != null ? newAuction.getAuctionEndTime() : LocalDateTime.now().plusDays(1));

            // Calculate starting bid
            if (newAuction.getStartingBidPrice() == null) {
                newAuction.setStartingBidPrice(itemDTO.getStartingBidPrice()); // Correctly retrieves startingBidPrice
            }

            // Initialize currentBidPrice
            newAuction.setCurrentBidPrice(newAuction.getStartingBidPrice());

            // Ensure auctionType is valid
            if (newAuction.getAuctionType() == null) {
                return new ResponseEntity<>("Auction type must be specified.", HttpStatus.BAD_REQUEST);
            }

            // Save the auction
            Auction savedAuction = auctionRepository.save(newAuction);

            // Broadcast via WebSocket
            try {
                this.auctionWSHandler.broadcast(String.valueOf(savedAuction.getId()), savedAuction);
            } catch (Exception e) {
                System.out.println("Unable to Send Updates");
            }

            return new ResponseEntity<>("Auction created successfully", HttpStatus.CREATED);

        } catch (FeignException.NotFound e) {
            // When Catalogue Service does not find the item...
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Item not found.");
        } catch (IllegalArgumentException e) {
            // When AuctionType is invalid...
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid auction type.");
        } catch (Exception e) {
            // Usual exception handling
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating auction.");
        }
    }

    /**
     * Places a bid on a forward auction.
     *
     * @param auctionId ID of the auction.
     * @param bid       Bid details.
     * @return ResponseEntity with the placed bid or an error message.
     */
    @PostMapping("/{auctionId}/bids")
    public ResponseEntity<?> placeForwardAuctionBid(@PathVariable Long auctionId, @RequestBody Bid bid) {
        try {
            // Check if the auction exists
            Optional<Auction> auctionOptional = auctionRepository.findById(auctionId);
            if (!auctionOptional.isPresent()) {
                return new ResponseEntity<>("Auction not found", HttpStatus.NOT_FOUND);
            }

            Auction auction = auctionOptional.get();

            // Check if auction is active and get type
            if (auction.getAuctionType() != AuctionType.FORWARD || auction.getStatus() != AuctionStatus.ACTIVE) {
                return new ResponseEntity<>("Item not available for bidding.", HttpStatus.BAD_REQUEST);
            }

            // Get the highest bid
            Bid highestBid = getHighestBidForAuction(auction.getId());

            // Check if new bid is higher
            if (highestBid != null && bid.getAmount() <= highestBid.getAmount()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Bid must be higher than the current highest bid.");
            }

            // Set auctionId and save bid
            bid.setAuctionId(auction.getId());
            Bid savedBid = bidRepository.save(bid);

            // Update auction's current bid price
            auction.setCurrentBidPrice(savedBid.getAmount());
            auction.calculateStatus();
            auctionRepository.save(auction);

            // Broadcast via WebSocket
            try {
                this.auctionWSHandler.broadcast(String.valueOf(auction.getId()), auction);
            } catch (Exception e) {
                System.out.println("Unable to Send Updates");
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(savedBid);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error placing bid.");
        }
    }

    /**
     * Places a 'Buy Now' bid on a Dutch auction.
     *
     * @param auctionId ID of the auction.
     * @param bid       Bid details.
     * @return ResponseEntity with the placed bid or an error message.
     */
    @PostMapping("/{auctionId}/buy-now")
    public ResponseEntity<?> placeDutchAuctionBid(@PathVariable Long auctionId, @RequestBody Bid bid) {

        Optional<Auction> optionalAuction = auctionRepository.findById(auctionId);
        if (!optionalAuction.isPresent()) {
            return new ResponseEntity<>("Auction not found.", HttpStatus.NOT_FOUND);
        }

        Auction auction = optionalAuction.get();
        // Check auction type and status
        if (auction.getAuctionType() != AuctionType.DUTCH || auction.getStatus() != AuctionStatus.ACTIVE) {
            return new ResponseEntity<>("Item not available for 'Buy Now'.", HttpStatus.BAD_REQUEST);
        }

        // Verify buyNowBid amount
        // Assuming buyNowPrice is the current bid price for simplicity
        if (bid.getAmount() == null || !bid.getAmount().equals(auction.getStartingBidPrice())) {
            return new ResponseEntity<>("Invalid bid amount for 'Buy Now'.", HttpStatus.BAD_REQUEST);
        }

        // Process the bid and auction update
        bid.setAuctionId(auction.getId());
        Bid savedBid = bidRepository.save(bid);
        auction.setCurrentBidPrice(bid.getAmount());
        auction.setStatus(AuctionStatus.SOLD); // Update auction status
        auctionRepository.save(auction);

        // Broadcast via WebSocket
        try {
            this.auctionWSHandler.broadcast(String.valueOf(auction.getId()), auction);
        } catch (Exception e) {
            System.out.println("Unable to Send Updates");
        }

        return new ResponseEntity<>(savedBid, HttpStatus.CREATED);
    }

    /**
     * Retrieves the highest bid for a specific auction.
     *
     * @param auctionId ID of the auction.
     * @return The highest bid or a NOT_FOUND status if no bids exist.
     */
    @GetMapping("/{auctionId}/bids/highest")
    public ResponseEntity<Bid> getHighestBidForItem(@PathVariable Long auctionId) {
        Optional<Auction> auction = auctionRepository.findById(auctionId);
        if (auction.isPresent()) {
            Bid highestBid = bidRepository.findByAuctionId(auction.get().getId()).stream()
                    .max(Comparator.comparing(Bid::getAmount))
                    .orElse(null);
            if (highestBid != null) {
                return new ResponseEntity<>(highestBid, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Retrieves the status of a specific auction.
     *
     * @param auctionId ID of the auction.
     * @return The auction details including its status or an error message.
     */
    @GetMapping("/{auctionId}/status")
    public ResponseEntity<?> getAuctionStatus(@PathVariable Long auctionId) {
        Optional<Auction> optionalAuction = auctionRepository.findById(auctionId);
        if (!optionalAuction.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Auction not found.");
        }

        Auction auction = optionalAuction.get();
        // Optionally, you can return just the status
        // return new ResponseEntity<>(auction.getStatus(), HttpStatus.OK);
        return new ResponseEntity<>(auction, HttpStatus.OK);
    }

    /**
     * Deletes a specific bid.
     *
     * @param bidId ID of the bid to delete.
     * @return ResponseEntity indicating success or failure.
     */
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

    /**
     * Deletes a specific auction.
     *
     * @param auctionId ID of the auction to delete.
     * @return ResponseEntity indicating success or failure.
     */
    @DeleteMapping("/{auctionId}")
    public ResponseEntity<?> deleteAuction(@PathVariable Long auctionId) {
        Optional<Auction> auctionOptional = auctionRepository.findById(auctionId);
        if (auctionOptional.isPresent()) {
            auctionRepository.deleteById(auctionId);
            return new ResponseEntity<>("Auction deleted successfully", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Auction not found", HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Helper method to retrieve the highest bid for an auction.
     *
     * @param auctionId ID of the auction.
     * @return The highest bid or null if none exists.
     */
    private Bid getHighestBidForAuction(Long auctionId) {
        return bidRepository.findByAuctionId(auctionId).stream()
                .max(Comparator.comparing(Bid::getAmount))
                .orElse(null);
    }
}
