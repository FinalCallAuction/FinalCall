// src/main/java/com/finalcall/catalogueservice/controller/ItemController.java

package com.finalcall.catalogueservice.controller;

import com.finalcall.catalogueservice.dto.ItemDTO;
import com.finalcall.catalogueservice.dto.ItemRequest;
import com.finalcall.catalogueservice.service.ItemService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
// import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/items")
@CrossOrigin(origins = "*")
public class ItemController {

    private static final Logger logger = LoggerFactory.getLogger(ItemController.class);

    @Autowired
    private ItemService itemService;

    /**
     * Create a new item and corresponding auction entry.
     *
     * @param itemRequest The item and auction details.
     * @param principal   JWT principal containing user details.
     * @return ResponseEntity with the created item or error message.
     */
    @PostMapping("/create")
    public ResponseEntity<?> createItem(@RequestBody ItemRequest itemRequest, @AuthenticationPrincipal Jwt principal) {
        Long userId;
        try {
            userId = Long.valueOf(principal.getSubject());
        } catch (NumberFormatException e) {
            logger.error("Invalid user ID in JWT: {}", principal.getSubject());
            return ResponseEntity.status(401).body("Invalid user ID.");
        }

        try {
            ItemDTO createdItem = itemService.createItemWithAuction(itemRequest, userId);
            return ResponseEntity.status(201).body(createdItem);
        } catch (Exception e) {
            logger.error("Error creating item and auction", e);
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    /**
     * Retrieve an item by its ID, including auction details and seller's name.
     *
     * @param id ID of the item.
     * @return ResponseEntity with the item details or error message.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getItemById(@PathVariable Long id) {
        try {
            ItemDTO itemDTO = itemService.getItemDetails(id);
            return ResponseEntity.ok().body(itemDTO);
        } catch (Exception e) {
            logger.error("Error fetching item by ID: {}", id, e);
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    /**
     * Upload additional images for an existing item.
     *
     * @param id         ID of the item.
     * @param imageFiles Array of image files to upload.
     * @param principal  JWT principal containing user details.
     * @return ResponseEntity with updated image URLs or error message.
     */
    @PostMapping("/{id}/upload-image")
    public ResponseEntity<?> uploadImage(
            @PathVariable Long id,
            @RequestParam("images") MultipartFile[] imageFiles,
            @AuthenticationPrincipal Jwt principal) {
        Long userId;
        try {
            userId = Long.valueOf(principal.getSubject());
        } catch (NumberFormatException e) {
            logger.error("Invalid user ID in JWT: {}", principal.getSubject());
            return ResponseEntity.status(401).body("Invalid user ID.");
        }

        try {
            List<String> imageUrls = itemService.uploadImages(id, imageFiles, userId);
            return ResponseEntity.ok(imageUrls);
        } catch (Exception e) {
            logger.error("Error uploading images for item ID: {}", id, e);
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    /**
     * Retrieve all items with their auction details and seller's name.
     *
     * @return ResponseEntity with list of items or error message.
     */
    @GetMapping
    public ResponseEntity<?> getAllItems() {
        try {
            List<ItemDTO> itemDTOs = itemService.getAllItemsWithDetails();
            return ResponseEntity.ok(itemDTOs);
        } catch (Exception e) {
            logger.error("Error fetching all items", e);
            return ResponseEntity.status(500).body("Error fetching items.");
        }
    }

    /**
     * Retrieve all items listed by the authenticated user.
     *
     * @param principal JWT principal containing user details.
     * @return ResponseEntity with list of items or error message.
     */
    @GetMapping("/user")
    public ResponseEntity<?> getUserItems(@AuthenticationPrincipal Jwt principal) {
        Long userId;
        try {
            userId = Long.valueOf(principal.getSubject());
        } catch (NumberFormatException e) {
            logger.error("Invalid user ID in JWT: {}", principal.getSubject());
            return ResponseEntity.status(401).body("Invalid user ID.");
        }

        try {
            List<ItemDTO> userItems = itemService.getUserItemsWithDetails(userId);
            return ResponseEntity.ok(userItems);
        } catch (Exception e) {
            logger.error("Error fetching user items", e);
            return ResponseEntity.status(500).body("Error fetching user items.");
        }
    }
}
