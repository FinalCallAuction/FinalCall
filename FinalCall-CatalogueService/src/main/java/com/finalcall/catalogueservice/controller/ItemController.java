package com.finalcall.catalogueservice.controller;

import com.finalcall.catalogueservice.entity.AuctionType;
import com.finalcall.catalogueservice.entity.Item;
import com.finalcall.catalogueservice.service.ItemService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.imageio.ImageIO;

@RestController
@RequestMapping("/api/items")
@CrossOrigin(origins = "*")
public class ItemController {

	private static final Logger logger = LoggerFactory.getLogger(ItemController.class);

    @Autowired
    private ItemService itemService;

    @Value("${image.upload.dir}")
    private String imageUploadDir;

    @PostMapping("/create")
    public ResponseEntity<?> createItem(
        @RequestParam("name") String name,
        @RequestParam("startingBid") BigDecimal startingBid,
        @RequestParam("auctionType") String auctionTypeStr,
        @RequestParam("auctionEndTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime auctionEndTime,
        @RequestParam(value = "images", required = false) MultipartFile[] imageFiles,
        @AuthenticationPrincipal Jwt principal
    ) {
        try {
            String username = principal.getSubject();
            AuctionType auctionType = AuctionType.valueOf(auctionTypeStr.toUpperCase());

            LocalDateTime nowPlusOneHour = LocalDateTime.now().plusHours(1);
            if (auctionEndTime.isBefore(nowPlusOneHour)) {
                logger.warn("Auction end time is too soon: {}", auctionEndTime);
                return ResponseEntity.badRequest().body("Auction end time must be at least 1 hour from now.");
            }

            Item item = new Item();
            item.setName(name);
            item.setStartingBid(startingBid);
            item.setAuctionType(auctionType);
            item.setAuctionEndTime(auctionEndTime);
            item.setListedBy(username);

            // Initialize imageUrls list if not already initialized
            if (item.getImageUrls() == null) {
                item.setImageUrls(new java.util.ArrayList<>());
            }

            // Persist the item first to ensure it has an ID/randomId
            item = itemService.createItem(item);
            logger.info("Created new item with ID: {}", item.getId());

            // Log the image upload directory
            logger.debug("Image upload directory: {}", imageUploadDir);

            // Handle multiple image uploads if present
            if (imageFiles != null && imageFiles.length > 0) {
                String itemImageDirPath = imageUploadDir + item.getRandomId() + "/";
                Path itemImageDirAbsolutePath = Paths.get(itemImageDirPath).toAbsolutePath();
                File itemImageDir = itemImageDirAbsolutePath.toFile();
                logger.debug("Resolved absolute path for image directory: {}", itemImageDir.getAbsolutePath());

                if (!itemImageDir.exists()) {
                    try {
                        boolean dirsCreated = itemImageDir.mkdirs();
                        if (!dirsCreated) {
                            logger.error("Failed to create upload directory at path: {}", itemImageDirPath);
                            return ResponseEntity.status(500).body("Failed to create upload directory.");
                        }
                        logger.info("Created upload directory at path: {}", itemImageDirPath);
                    } catch (SecurityException e) {
                        logger.error("Security exception while creating directory: {}", itemImageDirPath, e);
                        return ResponseEntity.status(500).body("Security exception while creating upload directory.");
                    }
                }

                // Handle image uploads and convert to PNG if necessary
                for (int i = 0; i < imageFiles.length; i++) {
                    MultipartFile imageFile = imageFiles[i];
                    if (!imageFile.isEmpty()) {
                        String newImageName = item.getRandomId() + "-" + (i + 1) + ".png"; // Ensure PNG format
                        Path imagePath = itemImageDirAbsolutePath.resolve(newImageName);
                        File dest = imagePath.toFile();

                        try {
                            BufferedImage bufferedImage = ImageIO.read(imageFile.getInputStream());
                            if (bufferedImage == null) {
                                logger.error("Failed to read image file: {}", imageFile.getOriginalFilename());
                                return ResponseEntity.status(400).body("Invalid image file.");
                            }

                            // Save image as PNG
                            ImageIO.write(bufferedImage, "png", dest);
                            logger.info("Saved image as PNG: {}", dest.getAbsolutePath());
                        } catch (IOException e) {
                            logger.error("Failed to save image file: {}", dest.getAbsolutePath(), e);
                            return ResponseEntity.status(500).body("Error saving image.");
                        }

                        // Construct the image URL
                        String imageUrl = "/itemimages/" + item.getRandomId() + "/" + newImageName;

                        // Add the image URL to the list
                        item.addImageUrl(imageUrl);
                        logger.info("Added image URL: {}", imageUrl);
                    }
                }

                // Save the updated item with image URLs
                itemService.saveItem(item);
                logger.info("Updated item with image URLs: {}", item.getImageUrls());
            }

            return ResponseEntity.ok(item);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid auction type: {}", auctionTypeStr, e);
            return ResponseEntity.badRequest().body("Invalid auction type. Valid types are DUTCH and FORWARD.");
        } catch (Exception e) {
            logger.error("Unexpected error during item creation", e);
            return ResponseEntity.status(500).body("An unexpected error occurred.");
        }
    }

    @PostMapping("/{id}/upload-image")
    public ResponseEntity<?> uploadImage(
            @PathVariable Long id,
            @RequestParam("images") MultipartFile[] imageFiles,
            @AuthenticationPrincipal Jwt principal) {
        try {
            String username = principal.getSubject();

            Optional<Item> itemOpt = itemService.getItemById(id);
            if (itemOpt.isEmpty()) {
                logger.warn("Item with ID {} not found", id);
                return ResponseEntity.status(404).body("Item not found.");
            }

            Item item = itemOpt.get();
            if (!item.getListedBy().equals(username)) {
                logger.warn("User {} is not authorized to upload images for item {}", username, id);
                return ResponseEntity.status(403).body("You are not authorized to upload images for this item.");
            }

            if (imageFiles != null && imageFiles.length > 0) {
                String itemImageDirPath = imageUploadDir + item.getRandomId() + "/";
                Path itemImageDirAbsolutePath = Paths.get(itemImageDirPath).toAbsolutePath();
                File itemImageDir = itemImageDirAbsolutePath.toFile();

                // Check if the image directory exists and create it if necessary
                if (!itemImageDir.exists()) {
                    if (!itemImageDir.mkdirs()) {
                        logger.error("Failed to create upload directory at path: {}", itemImageDirPath);
                        return ResponseEntity.status(500).body("Failed to create upload directory.");
                    }
                }

                // Count existing images in the folder to determine the starting index for new images
                int existingImageCount = item.getImageUrls().size();
                File[] existingFiles = itemImageDir.listFiles((dir, name) -> name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg"));
                if (existingFiles != null) {
                    existingImageCount = existingFiles.length;
                }

                for (int i = 0; i < imageFiles.length; i++) {
                    MultipartFile imageFile = imageFiles[i];
                    if (!imageFile.isEmpty()) {
                        String originalFilename = imageFile.getOriginalFilename();
                        String extension = (originalFilename != null && originalFilename.contains("."))
                                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                                : ".png"; // Default to .png if no extension

                        // Generate a new image name with a sequential index
                        String newImageName = item.getRandomId() + "-" + (existingImageCount + i + 1) + extension;
                        Path imagePath = itemImageDirAbsolutePath.resolve(newImageName);
                        File dest = imagePath.toFile();

                        try {
                            // Convert the image to PNG if necessary and save
                            BufferedImage bufferedImage = ImageIO.read(imageFile.getInputStream());
                            if (bufferedImage == null) {
                                logger.error("Failed to read image file: {}", imageFile.getOriginalFilename());
                                return ResponseEntity.status(400).body("Invalid image file.");
                            }
                            ImageIO.write(bufferedImage, "png", dest);
                            logger.info("Saved image as PNG: {}", dest.getAbsolutePath());
                        } catch (IOException e) {
                            logger.error("Failed to save image file: {}", dest.getAbsolutePath(), e);
                            return ResponseEntity.status(500).body("Error saving image.");
                        }

                        // Construct the image URL
                        String imageUrl = "/itemimages/" + item.getRandomId() + "/" + newImageName;

                        // Add the image URL to the list
                        item.addImageUrl(imageUrl);
                        logger.info("Added image URL: {}", imageUrl);
                    }
                }

                // Save the updated item with image URLs
                itemService.saveItem(item);
                logger.info("Updated item with image URLs: {}", item.getImageUrls());
                return ResponseEntity.ok(item.getImageUrls()); // Return updated image URLs to the front-end
            }

            return ResponseEntity.badRequest().body("No images to upload.");
        } catch (Exception e) {
            logger.error("Unexpected error during image upload", e);
            return ResponseEntity.status(500).body("Internal server error.");
        }
    }
    
    /**
     * Update images for an existing item.
     *
     * @param id         ID of the item to update.
     * @param updatedImages List of updated image URLs.
     * @param principal  JWT principal containing authenticated user details.
     * @return ResponseEntity indicating success or failure.
     */
    @PutMapping("/{id}/update-images")
    public ResponseEntity<?> updateImages(
        @PathVariable Long id,
        @RequestBody List<String> updatedImages,
        @AuthenticationPrincipal Jwt principal
    ) {
        try {
            String username = principal.getSubject();

            Optional<Item> itemOpt = itemService.getItemById(id);
            if (itemOpt.isEmpty()) {
                logger.warn("Item with ID {} not found", id);
                return ResponseEntity.status(404).body("Item not found.");
            }

            Item item = itemOpt.get();

            if (!item.getListedBy().equals(username)) {
                logger.warn("User {} is not authorized to update images for item {}", username, id);
                return ResponseEntity.status(403).body("You are not authorized to update images for this item.");
            }

            // Determine the item's image directory
            String itemImageDirPath = imageUploadDir + item.getRandomId() + "/";
            File itemImageDir = new File(itemImageDirPath);

            if (!itemImageDir.exists()) {
                return ResponseEntity.status(404).body("Item image directory not found.");
            }

            List<String> existingImageUrls = item.getImageUrls();
            List<String> newImageUrls = new ArrayList<>();

            // Handle deletions: Find images to delete
            for (String existingUrl : existingImageUrls) {
                if (!updatedImages.contains(existingUrl)) {
                    // Image has been removed, delete the file
                    String imageName = existingUrl.substring(existingUrl.lastIndexOf("/") + 1);
                    File imageFile = new File(itemImageDir, imageName);
                    if (imageFile.exists()) {
                        if (imageFile.delete()) {
                            logger.info("Deleted image file: {}", imageFile.getAbsolutePath());
                        } else {
                            logger.warn("Failed to delete image file: {}", imageFile.getAbsolutePath());
                            // Continue even if deletion fails
                        }
                    }
                }
            }

            // Now, process the updated images list
            for (int i = 0; i < updatedImages.size(); i++) {
                String updatedUrl = updatedImages.get(i);
                String imageName = updatedUrl.substring(updatedUrl.lastIndexOf("/") + 1);
                File imageFile = new File(itemImageDir, imageName);

                if (!imageFile.exists()) {
                    logger.warn("Image file {} does not exist, cannot update.", imageFile.getAbsolutePath());
                    return ResponseEntity.status(404).body("Image not found: " + updatedUrl);
                }

                // Rename the image file to match the new order
                String newImageName = item.getRandomId() + "-" + (i + 1) + ".png";
                File newImageFile = new File(itemImageDir, newImageName);

                if (!imageFile.getName().equals(newImageName)) {
                    if (imageFile.renameTo(newImageFile)) {
                        logger.info("Renamed image file from {} to {}", imageFile.getName(), newImageName);
                    } else {
                        logger.error("Failed to rename image file from {} to {}", imageFile.getName(), newImageName);
                        return ResponseEntity.status(500).body("Failed to rename image: " + imageName);
                    }
                }

                String newImageUrl = "/itemimages/" + item.getRandomId() + "/" + newImageName;
                newImageUrls.add(newImageUrl);
            }

            // Update the item's image URLs
            item.setImageUrls(newImageUrls);
            itemService.saveItem(item);
            logger.info("Updated item {} with new image URLs: {}", id, newImageUrls);

            // Return the updated image URLs list
            return ResponseEntity.ok(newImageUrls);
        } catch (Exception e) {
            logger.error("Unexpected error during image update for item {}", id, e);
            return ResponseEntity.status(500).body("Internal server error.");
        }
    }

    /**
     * Retrieve all active (ongoing) items.
     *
     * @return ResponseEntity with a list of active items or an error message.
     */
    @GetMapping
    public ResponseEntity<?> getAllActiveItems() {
        try {
            List<Item> items = itemService.getAllActiveItems();
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error.");
        }
    }

    /**
     * Retrieve items by auction type.
     *
     * @param auctionType Auction type as a string (e.g., "FORWARD" or "DUTCH").
     * @return ResponseEntity with a list of items matching the auction type or an error message.
     */
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

    /**
     * Retrieve an item by its ID.
     *
     * @param id ID of the item.
     * @return ResponseEntity with the item details or an error message.
     */
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

    /**
     * Update item details (excluding images).
     *
     * @param id          ID of the item to update.
     * @param updatedItem Item object containing updated details.
     * @param principal   JWT principal containing authenticated user details.
     * @return ResponseEntity with the updated item or an error message.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateItem(
        @PathVariable Long id,
        @RequestBody Item updatedItem,
        @AuthenticationPrincipal Jwt principal
    ) {
        try {
            // Optionally, verify if the user has permission to update the item
            Optional<Item> existingItemOpt = itemService.getItemById(id);
            if (existingItemOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Item not found.");
            }

            Item existingItem = existingItemOpt.get();

            // Optional: Verify that the authenticated user is the owner
            String username = principal.getSubject();
            if (!existingItem.getListedBy().equals(username)) {
                return ResponseEntity.status(403).body("You are not authorized to update this item.");
            }

            // Update fields (exclude fields that shouldn't be updated directly)
            existingItem.setName(updatedItem.getName());
            existingItem.setStartingBid(updatedItem.getStartingBid());
            existingItem.setAuctionType(updatedItem.getAuctionType());
            existingItem.setAuctionEndTime(updatedItem.getAuctionEndTime());
            // Add more fields as necessary

            // Save the updated item
            itemService.saveItem(existingItem);

            return ResponseEntity.ok(existingItem);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error.");
        }
    }
}
