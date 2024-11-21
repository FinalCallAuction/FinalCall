package com.finalcall.catalogueservice.controller;

import com.finalcall.catalogueservice.dto.AuctionDTO;
import com.finalcall.catalogueservice.dto.ItemRequest;
import com.finalcall.catalogueservice.entity.Item;
import com.finalcall.catalogueservice.repository.ItemRepository;
import com.finalcall.catalogueservice.service.ItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
    
    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${image.upload.dir}")
    private String imageUploadDir;

    @Value("${auction.service.url}")
    private String auctionServiceUrl; // http://localhost:8084

    public ItemController() {
        logger.info("ItemController initialized successfully.");
    }

    /**
     * Create a new item and corresponding auction entry.
     *
     * @param name          Name of the item.
     * @param startingBid   Starting bid price.
     * @param auctionType   Type of the auction (e.g., FORWARD, DUTCH).
     * @param auctionEndTime Auction end time in 'yyyy-MM-ddTHH:mm' format.
     * @param imageFiles    Array of image files.
     * @param principal     JWT principal containing user details.
     * @return ResponseEntity with the created item or error message.
     */
    @PostMapping("/create")
    public ResponseEntity<?> createItem(@RequestBody ItemRequest itemRequest, @AuthenticationPrincipal Jwt principal) {
        // Extract user ID from JWT
        Long userId = principal.getClaim("id");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User ID not found in token.");
        }

        // Create a new item entity and set fields
        Item item = new Item();
        item.setName(itemRequest.getName());
        item.setDescription(itemRequest.getDescription());
        item.setListedBy(userId);
        item.setStartingBidPrice(itemRequest.getStartingBid());

        // Save the item in CatalogueService database
        Item savedItem = itemRepository.save(item);

        // Prepare AuctionDTO to send to AuctionService
        AuctionDTO auctionDTO = new AuctionDTO();
        auctionDTO.setCatalogueItemId(savedItem.getId());
        auctionDTO.setAuctionType(itemRequest.getAuctionType());
        auctionDTO.setStartingBidPrice(itemRequest.getStartingBid());
        auctionDTO.setAuctionEndTime(itemRequest.getAuctionEndTime());

        // Get JWT token
        String jwtToken = principal.getTokenValue();

        // Send request to AuctionService
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(jwtToken);
            HttpEntity<AuctionDTO> request = new HttpEntity<>(auctionDTO, headers);

            restTemplate.postForEntity(auctionServiceUrl + "/api/auctions/create", request, Void.class);
        } catch (HttpClientErrorException.Forbidden e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Failed to communicate with Auction service: Unauthorized");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to communicate with Auction service");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(savedItem);
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
        try {
            Long userId = Long.parseLong(principal.getSubject()); // Assuming subject is userId

            Optional<Item> itemOpt = itemService.getItemById(id);
            if (itemOpt.isEmpty()) {
                logger.warn("Item with ID {} not found", id);
                return ResponseEntity.status(404).body("Item not found.");
            }

            Item item = itemOpt.get();
            if (!item.getListedBy().equals(userId)) {
                logger.warn("User {} is not authorized to upload images for item {}", userId, id);
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
     * @param id          ID of the item to update.
     * @param updatedImages List of updated image URLs.
     * @param principal  JWT principal containing user details.
     * @return ResponseEntity indicating success or failure.
     */
    @PutMapping("/{id}/update-images")
    public ResponseEntity<?> updateImages(
        @PathVariable Long id,
        @RequestBody List<String> updatedImages,
        @AuthenticationPrincipal Jwt principal
    ) {
        try {
            Long userId = Long.parseLong(principal.getSubject()); // Assuming subject is userId

            Optional<Item> itemOpt = itemService.getItemById(id);
            if (itemOpt.isEmpty()) {
                logger.warn("Item with ID {} not found", id);
                return ResponseEntity.status(404).body("Item not found.");
            }

            Item item = itemOpt.get();

            if (!item.getListedBy().equals(userId)) {
                logger.warn("User {} is not authorized to update images for item {}", userId, id);
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
     * Retrieve an item by its ID.
     *
     * @param id ID of the item.
     * @return ResponseEntity with the item details or error message.
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
     * Retrieve all items listed by the authenticated user.
     *
     * @param principal JWT principal containing user details.
     * @return ResponseEntity with list of items or error message.
     */
    @GetMapping("/user")
    public ResponseEntity<?> getUserItems(@AuthenticationPrincipal Jwt principal) {
        try {
            Long userId = principal.getClaim("id");
            if (userId == null) {
                logger.warn("JWT does not contain 'id' claim.");
                return ResponseEntity.status(400).body("Invalid token: missing user ID.");
            }

            List<Item> userItems = itemService.getItemsByUser(userId);
            return ResponseEntity.ok(userItems);
        } catch (Exception e) {
            logger.error("Error fetching user items", e);
            return ResponseEntity.status(500).body("Internal server error.");
        }
    }
    
    /**
     * Retrieve all items.
     *
     * @return ResponseEntity with the list of items or an error message.
     */
    @GetMapping
    public ResponseEntity<?> getAllItems() {
        try {
            List<Item> items = itemService.getAllItems();
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            logger.error("Error fetching all items", e);
            return ResponseEntity.status(500).body("Error fetching items.");
        }
    }

}
