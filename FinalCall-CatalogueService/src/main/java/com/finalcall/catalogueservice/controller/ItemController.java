package com.finalcall.catalogueservice.controller;

import com.finalcall.catalogueservice.client.AuctionServiceClient;
import com.finalcall.catalogueservice.client.AuthenticationServiceClient;
import com.finalcall.catalogueservice.dto.AuctionDTO;
import com.finalcall.catalogueservice.dto.ItemDTO;
import com.finalcall.catalogueservice.dto.ItemRequest;
import com.finalcall.catalogueservice.dto.UserDTO;
import com.finalcall.catalogueservice.entity.Item;
import com.finalcall.catalogueservice.exception.UserNotFoundException;
import com.finalcall.catalogueservice.service.ItemService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import javax.imageio.ImageIO;

@RestController
@RequestMapping("/api/items")
@CrossOrigin(origins = "*")
public class ItemController {

    private static final Logger logger = LoggerFactory.getLogger(ItemController.class);

    @Autowired
    private ItemService itemService;

    @Autowired
    private AuctionServiceClient auctionServiceClient;

    @Autowired
    private AuthenticationServiceClient authenticationServiceClient; // Inject AuthenticationServiceClient

    @Value("${image.upload.dir}")
    private String imageUploadDir;

    public ItemController() {
        logger.info("ItemController initialized successfully.");
    }

    /**
     * Create a new item and corresponding auction entry.
     *
     * @param itemRequest The item and auction details.
     * @param principal   JWT principal containing user details.
     * @return ResponseEntity with the created item or error message.
     */
    @PostMapping("/create")
    public ResponseEntity<?> createItem(@RequestBody ItemRequest itemRequest, @AuthenticationPrincipal Jwt principal) {
        // Extract user ID from JWT
        Long userId = principal.getClaim("id");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User ID not found in token.");
        }

        try {
            // Create Item
            Item item = new Item();
            item.setName(itemRequest.getName());
            item.setDescription(itemRequest.getDescription());
            item.setListedBy(userId);
            item.setStartingBidPrice(itemRequest.getStartingBid());

            // Save Item
            Item savedItem = itemService.createItem(item);

            // Communicate with AuctionService to create auction
            AuctionDTO auctionDTO = new AuctionDTO();
            auctionDTO.setItemId(savedItem.getId());
            auctionDTO.setAuctionType(itemRequest.getAuctionType());
            auctionDTO.setStartingBidPrice(itemRequest.getStartingBid());
            auctionDTO.setCurrentBidPrice(itemRequest.getStartingBid()); // Initialize currentBidPrice
            auctionDTO.setAuctionEndTime(itemRequest.getAuctionEndTime());
            auctionDTO.setSellerId(userId);
            auctionDTO.setStartTime(itemRequest.getAuctionStartTime());

            // If startTime is null, set it to current time
            if (auctionDTO.getStartTime() == null) {
                auctionDTO.setStartTime(LocalDateTime.now());
                logger.info("Auction startTime was null, set to current time: {}", auctionDTO.getStartTime());
            }

            // Log the AuctionDTO being sent
            logger.debug("Sending AuctionDTO to AuctionService: {}", auctionDTO);

            // Call AuctionService to create auction
            ResponseEntity<?> auctionResponse = auctionServiceClient.createAuction(auctionDTO);
            if (auctionResponse.getStatusCode() != HttpStatus.CREATED) {
                logger.error("AuctionService responded with status: {}", auctionResponse.getStatusCode());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating auction.");
            }

            // Fetch seller's name
            String sellerName = "Unknown";
            try {
                UserDTO userDTO = authenticationServiceClient.getUserById(userId);
                if (userDTO != null && userDTO.getUsername() != null) {
                    sellerName = userDTO.getUsername();
                }
            } catch (Exception e) {
                logger.error("Error fetching user with ID: {}", userId, e);
                // Optionally, handle specific exceptions (e.g., user not found)
            }

            // Build ItemDTO with auction details and seller's name
            ItemDTO itemDTO = new ItemDTO();
            itemDTO.setId(savedItem.getId());
            itemDTO.setRandomId(savedItem.getRandomId());
            itemDTO.setName(savedItem.getName());
            itemDTO.setDescription(savedItem.getDescription());
            itemDTO.setKeywords(savedItem.getKeywords());
            itemDTO.setImageUrls(savedItem.getImageUrls());
            itemDTO.setListedBy(savedItem.getListedBy());
            itemDTO.setListedByName(sellerName); // Set seller's name
            itemDTO.setStartingBidPrice(savedItem.getStartingBidPrice());
            itemDTO.setAuction(auctionDTO);

            return ResponseEntity.status(HttpStatus.CREATED).body(itemDTO);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid auction type: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid auction type: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error creating listing", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating listing: " + e.getMessage());
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
            Optional<Item> itemOpt = itemService.getItemById(id);
            if (itemOpt.isPresent()) {
                Item item = itemOpt.get();

                // Fetch auction details from AuctionService
                ResponseEntity<AuctionDTO> auctionResponse = auctionServiceClient.getAuctionByItemId(item.getId());
                AuctionDTO auctionDTO = null;
                if (auctionResponse.getStatusCode() == HttpStatus.OK) {
                    auctionDTO = auctionResponse.getBody();
                }

                // Fetch seller's name from AuthenticationService
                String sellerName = "Unknown";
                if (item.getListedBy() != null) {
                    try {
                        UserDTO userDTO = authenticationServiceClient.getUserById(item.getListedBy());
                        if (userDTO != null && userDTO.getUsername() != null) { // Assuming 'name' is 'username'
                            sellerName = userDTO.getUsername();
                        }
                    } catch (Exception e) {
                        logger.error("Error fetching user with ID: {}", item.getListedBy(), e);
                        // Optionally, handle specific exceptions (e.g., user not found)
                    }
                }

                // Build ItemDTO with auction details and seller's name
                ItemDTO itemDTO = new ItemDTO();
                itemDTO.setId(item.getId());
                itemDTO.setRandomId(item.getRandomId());
                itemDTO.setName(item.getName());
                itemDTO.setDescription(item.getDescription());
                itemDTO.setKeywords(item.getKeywords());
                itemDTO.setImageUrls(item.getImageUrls());
                itemDTO.setListedBy(item.getListedBy());
                itemDTO.setListedByName(sellerName); // Set seller's name
                itemDTO.setStartingBidPrice(item.getStartingBidPrice());
                itemDTO.setAuction(auctionDTO);

                return ResponseEntity.ok().body(itemDTO);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Item not found.");
            }
        } catch (Exception e) {
            logger.error("Error fetching item by ID", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error.");
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
        try {
            Long userId = principal.getClaim("id");

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

                // Check if image directory exists, create if necessary
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

                        // Generate new image name with a sequential index
                        String newImageName = item.getRandomId() + "-" + (existingImageCount + i + 1) + extension;
                        Path imagePath = itemImageDirAbsolutePath.resolve(newImageName);
                        File dest = imagePath.toFile();

                        // Convert the image to PNG if necessary and save
                        try {
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
                return ResponseEntity.ok(item.getImageUrls());
            }

            return ResponseEntity.badRequest().body("No images to upload.");
        } catch (Exception e) {
            logger.error("Unexpected error during image upload", e);
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
            List<ItemDTO> userItemDTOs = new ArrayList<>();

            for (Item item : userItems) {
                // Fetch auction details
                ResponseEntity<AuctionDTO> auctionResponse = auctionServiceClient.getAuctionByItemId(item.getId());
                AuctionDTO auctionDTO = null;
                if (auctionResponse.getStatusCode() == HttpStatus.OK) {
                    auctionDTO = auctionResponse.getBody();
                }

                // Fetch seller's name
                String sellerName = "Unknown";
                if (item.getListedBy() != null) {
                    try {
                        UserDTO userDTO = authenticationServiceClient.getUserById(item.getListedBy());
                        if (userDTO != null && userDTO.getUsername() != null) { // Assuming 'username' is the name to display
                            sellerName = userDTO.getUsername();
                        }
                    } catch (UserNotFoundException e) {
                        logger.error("User not found with ID: {}", item.getListedBy(), e);
                        // Optionally, set to "Unknown" or handle as needed
                    } catch (Exception e) {
                        logger.error("Error fetching user with ID: {}", item.getListedBy(), e);
                        // Optionally, set to "Unknown" or handle as needed
                    }
                }

                // Build ItemDTO
                ItemDTO itemDTO = new ItemDTO();
                itemDTO.setId(item.getId());
                itemDTO.setRandomId(item.getRandomId());
                itemDTO.setName(item.getName());
                itemDTO.setDescription(item.getDescription());
                itemDTO.setKeywords(item.getKeywords());
                itemDTO.setImageUrls(item.getImageUrls());
                itemDTO.setListedBy(item.getListedBy());
                itemDTO.setListedByName(sellerName);
                itemDTO.setStartingBidPrice(item.getStartingBidPrice());
                itemDTO.setAuction(auctionDTO);

                userItemDTOs.add(itemDTO);
            }

            return ResponseEntity.ok(userItemDTOs);
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
            List<ItemDTO> itemDTOs = new ArrayList<>();

            for (Item item : items) {
                // Fetch auction details
                ResponseEntity<AuctionDTO> auctionResponse = auctionServiceClient.getAuctionByItemId(item.getId());
                AuctionDTO auctionDTO = null;
                if (auctionResponse.getStatusCode() == HttpStatus.OK) {
                    auctionDTO = auctionResponse.getBody();
                }

                // Fetch seller's name
                String sellerName = "Unknown";
                if (item.getListedBy() != null) {
                    try {
                        UserDTO userDTO = authenticationServiceClient.getUserById(item.getListedBy());
                        if (userDTO != null && userDTO.getUsername() != null) { // Assuming 'username' is the name to display
                            sellerName = userDTO.getUsername();
                        }
                    } catch (UserNotFoundException e) {
                        logger.error("User not found with ID: {}", item.getListedBy(), e);
                        // Optionally, set to "Unknown" or handle as needed
                    } catch (Exception e) {
                        logger.error("Error fetching user with ID: {}", item.getListedBy(), e);
                        // Optionally, set to "Unknown" or handle as needed
                    }
                }

                // Build ItemDTO
                ItemDTO itemDTO = new ItemDTO();
                itemDTO.setId(item.getId());
                itemDTO.setRandomId(item.getRandomId());
                itemDTO.setName(item.getName());
                itemDTO.setDescription(item.getDescription());
                itemDTO.setKeywords(item.getKeywords());
                itemDTO.setImageUrls(item.getImageUrls());
                itemDTO.setListedBy(item.getListedBy());
                itemDTO.setListedByName(sellerName);
                itemDTO.setStartingBidPrice(item.getStartingBidPrice());
                itemDTO.setAuction(auctionDTO);

                itemDTOs.add(itemDTO);
            }

            return ResponseEntity.ok(itemDTOs);
        } catch (Exception e) {
            logger.error("Error fetching all items", e);
            return ResponseEntity.status(500).body("Error fetching items.");
        }
    }
}
