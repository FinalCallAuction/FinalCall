package com.finalcall.backend.controller;

import com.finalcall.backend.entity.Item;
import com.finalcall.backend.entity.AuctionType;
import com.finalcall.backend.service.ItemService;
import com.finalcall.backend.entity.User;
import com.finalcall.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/items")
@CrossOrigin(origins = "http://localhost:3000") // Adjust as per frontend port
public class ItemController {

    private static final Logger logger = LoggerFactory.getLogger(ItemController.class);

    @Autowired
    private ItemService itemService;
    
    @Autowired
    private UserService userService;

    // Updated Upload Directory
    private final String UPLOAD_DIR = System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "itemimages" + File.separator;

    /**
     * Get all active items (auctions that haven't ended).
     */
    @GetMapping("/")
    public ResponseEntity<?> getAllActiveItems() {
        logger.debug("Received request to fetch all active items.");
        try {
            List<Item> items = itemService.getAllActiveItems();
            logger.info("Fetched {} active items.", items.size());
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            logger.error("Error fetching active items: {}", e.getMessage());
            return ResponseEntity.status(500).body("Internal server error.");
        }
    }

    /**
     * Get items by auction type (DUTCH or FORWARD).
     * Example: /api/items/type/DUTCH
     */
    @GetMapping("/type/{auctionType}")
    public ResponseEntity<?> getItemsByAuctionType(@PathVariable String auctionType) {
        logger.debug("Received request to fetch items by auction type: {}", auctionType);
        try {
            AuctionType type = AuctionType.valueOf(auctionType.toUpperCase());
            List<Item> items = itemService.getItemsByAuctionType(type);
            logger.info("Fetched {} items of auction type {}.", items.size(), type);
            return ResponseEntity.ok(items);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid auction type provided: {}", auctionType);
            return ResponseEntity.badRequest().body("Invalid auction type. Valid types are DUTCH and FORWARD.");
        } catch (Exception e) {
            logger.error("Error fetching items by auction type {}: {}", auctionType, e.getMessage());
            return ResponseEntity.status(500).body("Internal server error.");
        }
    }

    /**
     * Get item details by ID.
     * Example: /api/items/1
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getItemById(@PathVariable Long id) {
        logger.debug("Received request to fetch item with ID: {}", id);
        try {
            Optional<Item> itemOpt = itemService.getItemById(id);
            if (itemOpt.isPresent()) {
                logger.info("Item found: {}", itemOpt.get());
                return ResponseEntity.ok(itemOpt.get());
            } else {
                logger.warn("Item with ID {} not found.", id);
                return ResponseEntity.status(404).body("Item not found.");
            }
        } catch (Exception e) {
            logger.error("Error fetching item with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(500).body("Internal server error.");
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createItem(
        @RequestParam("name") String name,
        @RequestParam("startingBid") Double startingBid,
        @RequestParam("auctionType") String auctionTypeStr,
        @RequestParam("auctionEndTime") String auctionEndTime,
        @RequestParam("userId") Long userId,
        @RequestParam(value = "image", required = false) MultipartFile image
    ) {
        logger.debug("Received request to create item: name={}, startingBid={}, auctionType={}, auctionEndTime={}, userId={}", 
            name, startingBid, auctionTypeStr, auctionEndTime, userId);
        try {
            AuctionType auctionType = AuctionType.valueOf(auctionTypeStr.toUpperCase());

            // Parse the auctionEndTime using ISO_LOCAL_DATE_TIME
            // assuming the format is "YYYY-MM-DDTHH:mm:ss" (e.g., 2024-11-15T05:28:00)
            LocalDateTime localEndDateTime = LocalDateTime.parse(auctionEndTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            // Additional Validation: Ensure auctionEndTime is at least 1 hour from now
            LocalDateTime nowPlusOneHour = LocalDateTime.now().plusHours(1);
            if (localEndDateTime.isBefore(nowPlusOneHour)) {
                logger.warn("Auction end time {} is less than 1 hour from now.", localEndDateTime);
                return ResponseEntity.badRequest().body("Auction end time must be at least 1 hour from now.");
            }

            Optional<User> userOpt = userService.getUserById(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("User not found.");
            }
            User user = userOpt.get();

            // Create the item without imageUrl
            Item item = itemService.createItem(name, startingBid, auctionType, localEndDateTime, userId, null);
            logger.info("Item created successfully: {}", item);

            String imageUrl = null;
            if (image != null && !image.isEmpty()) {
                // Define the item's image directory
                String itemImageDirPath = UPLOAD_DIR + item.getRandomId() + File.separator;
                File itemImageDir = new File(itemImageDirPath);
                if (!itemImageDir.exists()) {
                    boolean dirsCreated = itemImageDir.mkdirs();
                    if (!dirsCreated) {
                        logger.error("Failed to create upload directory at {}", itemImageDirPath);
                        return ResponseEntity.status(500).body("Failed to create upload directory.");
                    }
                }

                // Determine the image number (assuming single image for now)
                int imageNumber = 1;

                // Get the file extension
                String originalFilename = image.getOriginalFilename();
                String extension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }

                String newImageName = item.getRandomId() + "-" + imageNumber + extension;
                File dest = new File(itemImageDir, newImageName);
                image.transferTo(dest);
                imageUrl = "/uploads/" + item.getRandomId() + "/" + newImageName;

                // Update the item with imageUrl
                item.setImageUrl(imageUrl);
                itemService.saveItem(item);

                logger.debug("Image uploaded successfully to: {}", imageUrl);
            }

            return ResponseEntity.ok(item);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid auction type: {}", auctionTypeStr);
            return ResponseEntity.badRequest().body("Invalid auction type. Valid types are DUTCH and FORWARD.");
        } catch (IOException e) {
            logger.error("Error saving image: {}", e.getMessage());
            return ResponseEntity.status(500).body("Error saving image.");
        } catch (Exception e) {
            logger.error("Error creating item: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }



    /**
     * Place a bid on an item.
     * Endpoint: POST /api/items/{id}/bid
     */
    @PostMapping("/{id}/bid")
    public ResponseEntity<?> placeBid(@PathVariable Long id, @RequestBody BidRequest bidRequest) {
        logger.debug("Received bid request on item ID {}: {}", id, bidRequest);
        try {
            // Fetch item
            Item item = itemService.getItemById(id)
                .orElseThrow(() -> new Exception("Item not found."));
            logger.debug("Item fetched: {}", item);

            // Check if auction is active
            if (item.getAuctionEndTime().isBefore(LocalDateTime.now())) {
                logger.warn("Attempt to bid on ended auction: Item ID {}", id);
                return ResponseEntity.badRequest().body("Auction has ended.");
            }

            // Check if bidder is the lister
            if (item.getListedBy().getId().equals(bidRequest.getBidderId())) {
                logger.warn("User ID {} attempted to bid on their own item ID {}", bidRequest.getBidderId(), id);
                return ResponseEntity.badRequest().body("You cannot bid on your own item.");
            }

            // Handle auction type logic
            if (item.getAuctionType() == AuctionType.FORWARD) {
                // In a Forward auction, accepting the current bid ends the auction
                handleForwardAuctionBid(item, bidRequest);
            } else if (item.getAuctionType() == AuctionType.DUTCH) {
                // Implement Dutch auction bid logic if necessary
                handleDutchAuctionBid(item, bidRequest);
            } else {
                logger.error("Unsupported auction type: {}", item.getAuctionType());
                return ResponseEntity.badRequest().body("Unsupported auction type.");
            }

            logger.info("Bid placed successfully on item ID {}", id);
            return ResponseEntity.ok("Bid placed successfully.");
        } catch (Exception e) {
            logger.error("Error placing bid on item ID {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Handle bid placement for Forward Auctions.
     * Accepting the current bid ends the auction.
     */
    private void handleForwardAuctionBid(Item item, BidRequest bidRequest) throws Exception {
        logger.debug("Handling Forward Auction Bid: {}", bidRequest);
        
        // In a Forward auction, the bidAmount is the amount the bidder accepts.
        // Update the current bid and end the auction by setting auctionEndTime to now.
        if (bidRequest.getBidAmount() < item.getCurrentBid()) {
            logger.warn("Bid amount ${} is lower than current bid ${} for item ID {}", 
                        bidRequest.getBidAmount(), item.getCurrentBid(), item.getId());
            throw new Exception("Bid amount must be equal to or higher than the current bid.");
        }

        // Update the current bid
        item.setCurrentBid(bidRequest.getBidAmount());
        // End the auction
        item.setAuctionEndTime(LocalDateTime.now());
        itemService.saveItem(item);
        logger.debug("Forward auction bid processed: {}", item);
    }

    /**
     * Handle bid placement for Dutch Auctions.
     * For simplicity, this example treats it similarly to Forward auctions.
     * Customize as needed for actual Dutch auction logic.
     */
    private void handleDutchAuctionBid(Item item, BidRequest bidRequest) throws Exception {
        logger.debug("Handling Dutch Auction Bid: {}", bidRequest);
        
        // In a Dutch auction, the bidder accepts the current bid price.
        // Update the current bid and end the auction.
        if (bidRequest.getBidAmount() != item.getCurrentBid()) {
            logger.warn("Bid amount ${} does not match current bid ${} for Dutch auction item ID {}", 
                        bidRequest.getBidAmount(), item.getCurrentBid(), item.getId());
            throw new Exception("In a Dutch auction, bid amount must match the current bid.");
        }

        // Update the current bid (though it's the same in this simplified logic)
        item.setCurrentBid(bidRequest.getBidAmount());
        // End the auction
        item.setAuctionEndTime(LocalDateTime.now());
        itemService.saveItem(item);
        logger.debug("Dutch auction bid processed: {}", item);
    }

    /**
     * DTO for Bid Request
     */
    static class BidRequest {
        private Double bidAmount;
        private Long bidderId; // ID of the user placing the bid

        // Getters and Setters

        public Double getBidAmount() {
            return bidAmount;
        }

        public Long getBidderId() {
            return bidderId;
        }

        public void setBidAmount(Double bidAmount) {
            this.bidAmount = bidAmount;
        }

        public void setBidderId(Long bidderId) {
            this.bidderId = bidderId;
        }

        @Override
        public String toString() {
            return "BidRequest{" +
                    "bidAmount=" + bidAmount +
                    ", bidderId=" + bidderId +
                    '}';
        }
    }
}
