package com.finalcall.catalogueservice.controller;

import com.finalcall.catalogueservice.entity.AuctionType;
import com.finalcall.catalogueservice.entity.Item;
import com.finalcall.catalogueservice.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/items")
@CrossOrigin(origins = "*")
public class ItemController {

    @Autowired
    private ItemService itemService;

    // Get all active items - Removed trailing slash
    @GetMapping
    public ResponseEntity<?> getAllActiveItems() {
        try {
            List<Item> items = itemService.getAllActiveItems();
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error.");
        }
    }

    // Get items by auction type
    @GetMapping("/type/{auctionType}")
    public ResponseEntity<?> getItemsByAuctionType(@PathVariable String auctionType) {
        try {
            AuctionType type = AuctionType.valueOf(auctionType.toUpperCase());
            List<Item> items = itemService.getItemsByAuctionType(type);
            return ResponseEntity.ok(items);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid auction type. Valid types are DUTCH and FORWARD.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error.");
        }
    }

    // Get item by ID
    @GetMapping("/{id}")
    public ResponseEntity<Object> getItemById(@PathVariable Long id) {
        try {
            Optional<Item> itemOpt = itemService.getItemById(id);
            return itemOpt
                    .map(item -> ResponseEntity.ok().body((Object) item))
                    .orElseGet(() -> ResponseEntity.status(404).body((Object) "Item not found."));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error.");
        }
    }

    // Create a new item
    @PostMapping("/create")
    public ResponseEntity<?> createItem(
        @RequestParam("name") String name,
        @RequestParam("startingBid") BigDecimal startingBid,
        @RequestParam("auctionType") String auctionTypeStr,
        @RequestParam("auctionEndTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime auctionEndTime,
        @RequestParam(value = "image", required = false) MultipartFile imageFile,
        @AuthenticationPrincipal Jwt principal
    ) {
        try {
            String username = principal.getSubject();

            AuctionType auctionType = AuctionType.valueOf(auctionTypeStr.toUpperCase());

            LocalDateTime nowPlusOneHour = LocalDateTime.now().plusHours(1);
            if (auctionEndTime.isBefore(nowPlusOneHour)) {
                return ResponseEntity.badRequest().body("Auction end time must be at least 1 hour from now.");
            }

            Item item = new Item();
            item.setName(name);
            item.setStartingBid(startingBid);
            item.setAuctionType(auctionType);
            item.setAuctionEndTime(auctionEndTime);
            item.setListedBy(username);

            item = itemService.createItem(item);

            // Handle image upload if present
            if (imageFile != null && !imageFile.isEmpty()) {
                String itemImageDirPath = "src/main/resources/itemimages/" + item.getRandomId() + "/";
                File itemImageDir = new File(itemImageDirPath);
                if (!itemImageDir.exists()) {
                    boolean dirsCreated = itemImageDir.mkdirs();
                    if (!dirsCreated) {
                        return ResponseEntity.status(500).body("Failed to create upload directory.");
                    }
                }

                int imageNumber = 1;
                String originalFilename = imageFile.getOriginalFilename();
                String extension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }

                String newImageName = item.getRandomId() + "-" + imageNumber + extension;
                File dest = new File(itemImageDir, newImageName);
                imageFile.transferTo(dest);
                String imageUrl = "/itemimages/" + item.getRandomId() + "/" + newImageName;

                item.setImageUrl(imageUrl);
                itemService.saveItem(item);
            }

            return ResponseEntity.ok(item);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid auction type. Valid types are DUTCH and FORWARD.");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error saving image.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Place a bid
    @PostMapping("/{id}/bid")
    public ResponseEntity<?> placeBid(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        try {
            BigDecimal bidAmount = new BigDecimal(payload.get("bidAmount").toString());
            Long bidderId = Long.parseLong(payload.get("bidderId").toString());
            Item item = itemService.placeBid(id, bidAmount, bidderId);
            return ResponseEntity.ok(item);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Accept the current bid by the lister
    @PostMapping("/{id}/acceptBid")
    public ResponseEntity<?> acceptBid(@PathVariable Long id, @RequestBody Map<String, Object> payload, @AuthenticationPrincipal Jwt principal) {
        try {
            String username = principal.getSubject();
            itemService.acceptBid(id, username);
            return ResponseEntity.ok("Bid accepted successfully.");
        } catch (Exception e) {
            if (e.getMessage().equals("Item not found.")) {
                return ResponseEntity.status(404).body(e.getMessage());
            } else if (e.getMessage().equals("You are not authorized to accept bids for this item.")) {
                return ResponseEntity.status(403).body(e.getMessage());
            } else if (e.getMessage().equals("No valid bids to accept.")) {
                return ResponseEntity.badRequest().body(e.getMessage());
            } else {
                return ResponseEntity.status(500).body("Internal server error.");
            }
        }
    }

 // Get active listings for the current user
    @GetMapping("/user/active-listings")
    public ResponseEntity<?> getActiveListingsByUser(@AuthenticationPrincipal Jwt principal) {
        try {
            String username = principal.getSubject();
            List<Item> items = itemService.getActiveListingsByUser(username);
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error.");
        }
    }

}
