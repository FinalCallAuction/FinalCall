// src/main/java/com/finalcall/catalogueservice/service/ItemService.java

package com.finalcall.catalogueservice.service;

import com.finalcall.catalogueservice.client.AuctionServiceClient;
import com.finalcall.catalogueservice.client.AuthenticationServiceClient;
import com.finalcall.catalogueservice.dto.AuctionDTO;
import com.finalcall.catalogueservice.dto.ItemDTO;
import com.finalcall.catalogueservice.dto.ItemRequest;
import com.finalcall.catalogueservice.dto.UserDTO;
import com.finalcall.catalogueservice.entity.Item;
import com.finalcall.catalogueservice.exception.UserNotFoundException;
import com.finalcall.catalogueservice.repository.ItemRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

// import org.springframework.transaction.annotation.Transactional;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

import javax.imageio.ImageIO;

@Service
public class ItemService {

    private static final Logger logger = LoggerFactory.getLogger(ItemService.class);

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private AuctionServiceClient auctionServiceClient;

    @Autowired
    private AuthenticationServiceClient authenticationServiceClient;

    @Value("${image.upload.dir}")
    private String imageUploadDir;

    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int RANDOM_ID_LENGTH = 8;
    private Random random = new Random();

    private String generateRandomId() {
        StringBuilder sb = new StringBuilder(RANDOM_ID_LENGTH);
        for (int i = 0; i < RANDOM_ID_LENGTH; i++) {
            sb.append(ALPHANUMERIC.charAt(random.nextInt(ALPHANUMERIC.length())));
        }
        return sb.toString();
    }

    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    public Optional<Item> getItemById(Long id) {
        return itemRepository.findById(id);
    }

    public List<Item> getItemsByUser(Long listedBy) {
        return itemRepository.findByListedBy(listedBy);
    }

    /**
     * Creates a new item and its corresponding auction.
     *
     * @param itemRequest The request containing item and auction details.
     * @param userId      The ID of the user creating the item.
     * @return The created ItemDTO.
     * @throws Exception if any error occurs during creation.
     */
    @Transactional
    public ItemDTO createItemWithAuction(ItemRequest itemRequest, Long userId) throws Exception {
        // Create Item entity
        Item item = new Item();
        item.setName(itemRequest.getName());
        item.setDescription(itemRequest.getDescription());
        item.setListedBy(userId);
        item.setStartingBidPrice(itemRequest.getStartingBid());

        // Save Item
        Item savedItem = createItem(item);

        // Create AuctionDTO
        AuctionDTO auctionDTO = new AuctionDTO();
        auctionDTO.setItemId(savedItem.getId());
        auctionDTO.setAuctionType(itemRequest.getAuctionType());
        auctionDTO.setStartingBidPrice(itemRequest.getStartingBid());
        auctionDTO.setCurrentBidPrice(itemRequest.getStartingBid());
        auctionDTO.setAuctionEndTime(itemRequest.getAuctionEndTime());
        auctionDTO.setSellerId(userId);
        auctionDTO.setStartTime(itemRequest.getAuctionStartTime() != null ? itemRequest.getAuctionStartTime() : LocalDateTime.now());

        // Create Auction via AuctionService
        ResponseEntity<?> auctionResponse = auctionServiceClient.createAuction(auctionDTO);
        if (auctionResponse.getStatusCode() != HttpStatus.CREATED) {
            logger.error("Failed to create auction: {}", auctionResponse.getStatusCode());
            throw new Exception("Failed to create auction.");
        }

        // Fetch seller's name from AuthenticationService
        String sellerName = fetchSellerName(userId);

        // Build ItemDTO
        ItemDTO itemDTO = mapToItemDTO(savedItem, auctionDTO, sellerName);

        return itemDTO;
    }

    private String fetchSellerName(Long userId) {
        try {
            UserDTO userDTO = authenticationServiceClient.getUserById(userId);
            if (userDTO != null && userDTO.getUsername() != null) {
                return userDTO.getUsername();
            }
        } catch (UserNotFoundException e) {
            logger.warn("User not found with ID: {}", userId);
        } catch (Exception e) {
            logger.error("Error fetching user with ID: {}", userId, e);
        }
        return "Unknown";
    }

    private ItemDTO mapToItemDTO(Item item, AuctionDTO auctionDTO, String sellerName) {
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
        return itemDTO;
    }

    /**
     * Creates and saves an Item entity, ensuring unique randomId.
     *
     * @param item The Item entity to create.
     * @return The saved Item entity.
     */
    public Item createItem(Item item) {
        String randomId;
        do {
            randomId = generateRandomId();
        } while (itemRepository.existsByRandomId(randomId));
        item.setRandomId(randomId);
        return itemRepository.save(item);
    }

    public Item saveItem(Item item) {
        return itemRepository.save(item);
    }

    /**
     * Retrieves all items with their auction details and seller's name.
     *
     * @return List of ItemDTOs.
     */
//    public List<ItemDTO> getAllItemsWithDetails() {
//        List<Item> items = getAllItems();
//        List<ItemDTO> itemDTOs = new ArrayList<>();
//
//        for (Item item : items) {
//            try {
//                // Fetch auction details
//                ResponseEntity<AuctionDTO> auctionResponse = auctionServiceClient.getAuctionByItemId(item.getId());
//                AuctionDTO auctionDTO = null;
//                if (auctionResponse.getStatusCode() == HttpStatus.OK) {
//                    auctionDTO = auctionResponse.getBody();
//                }
//
//                // Fetch seller's name
//                String sellerName = fetchSellerName(item.getListedBy());
//
//                // Map to ItemDTO
//                ItemDTO itemDTO = mapToItemDTO(item, auctionDTO, sellerName);
//                itemDTOs.add(itemDTO);
//            } catch (Exception e) {
//                logger.error("Error fetching auction details for item ID: {}", item.getId(), e);
//                // Optionally, continue or handle differently
//            }
//        }
//
//        return itemDTOs;
//    }

    public List<ItemDTO> getAllItemsWithDetails() {
        List<Item> items = getAllItems();
        logger.debug("Found {} items in total", items.size());

        List<ItemDTO> itemDTOs = new ArrayList<>();

        for (Item item : items) {
            try {
                logger.debug("Processing item ID: {}", item.getId());
                
                // Fetch auction details
                ResponseEntity<AuctionDTO> auctionResponse = auctionServiceClient.getAuctionByItemId(item.getId());
                logger.debug("Auction response for item {}: {}", item.getId(), auctionResponse.getStatusCode());
                
                AuctionDTO auctionDTO = null;
                if (auctionResponse.getStatusCode() == HttpStatus.OK) {
                    auctionDTO = auctionResponse.getBody();
                    logger.debug("Retrieved auction details for item {}", item.getId());
                }

                // Fetch seller's name
                String sellerName = fetchSellerName(item.getListedBy());
                logger.debug("Retrieved seller name for item {}: {}", item.getId(), sellerName);

                // Map to ItemDTO
                ItemDTO itemDTO = mapToItemDTO(item, auctionDTO, sellerName);
                itemDTOs.add(itemDTO);
                logger.debug("Successfully mapped item {} to DTO", item.getId());
            } catch (Exception e) {
                logger.error("Error processing item ID: {} - Error: {}", item.getId(), e.getMessage(), e);
            }
        }

        logger.debug("Returning {} processed items", itemDTOs.size());
        return itemDTOs;
    }
    
    /**
     * Retrieves an Item with its auction details and seller's name.
     *
     * @param id The ID of the item.
     * @return ItemDTO if found, else throws Exception.
     * @throws Exception if item not found or any error occurs.
     */
    public ItemDTO getItemDetails(Long id) throws Exception {
        Optional<Item> itemOpt = getItemById(id);
        if (itemOpt.isEmpty()) {
            throw new Exception("Item not found.");
        }

        Item item = itemOpt.get();

        // Fetch auction details
        ResponseEntity<AuctionDTO> auctionResponse = auctionServiceClient.getAuctionByItemId(item.getId());
        AuctionDTO auctionDTO = null;
        if (auctionResponse.getStatusCode() == HttpStatus.OK) {
            auctionDTO = auctionResponse.getBody();
        }

        // Fetch seller's name
        String sellerName = fetchSellerName(item.getListedBy());

        // Map to ItemDTO
        ItemDTO itemDTO = mapToItemDTO(item, auctionDTO, sellerName);

        return itemDTO;
    }

    /**
     * Retrieves all items listed by a specific user with auction details and seller's name.
     *
     * @param userId The ID of the user.
     * @return List of ItemDTOs.
     */
    public List<ItemDTO> getUserItemsWithDetails(Long userId) {
        List<Item> items = getItemsByUser(userId);
        List<ItemDTO> itemDTOs = new ArrayList<>();

        for (Item item : items) {
            try {
                // Fetch auction details
                ResponseEntity<AuctionDTO> auctionResponse = auctionServiceClient.getAuctionByItemId(item.getId());
                AuctionDTO auctionDTO = null;
                if (auctionResponse.getStatusCode() == HttpStatus.OK) {
                    auctionDTO = auctionResponse.getBody();
                }

                // Fetch seller's name
                String sellerName = fetchSellerName(item.getListedBy());

                // Map to ItemDTO
                ItemDTO itemDTO = mapToItemDTO(item, auctionDTO, sellerName);
                itemDTOs.add(itemDTO);
            } catch (Exception e) {
                logger.error("Error fetching auction details for user item ID: {}", item.getId(), e);
                // Optionally, continue or handle differently
            }
        }

        return itemDTOs;
    }

    /**
     * Uploads images for an existing item.
     *
     * @param id         ID of the item.
     * @param imageFiles Array of image files.
     * @param userId     ID of the user uploading images.
     * @return List of image URLs.
     * @throws Exception if any error occurs.
     */
    @Transactional
    public List<String> uploadImages(Long id, MultipartFile[] imageFiles, Long userId) throws Exception {
        Optional<Item> itemOpt = getItemById(id);
        if (itemOpt.isEmpty()) {
            throw new Exception("Item not found.");
        }

        Item item = itemOpt.get();

        if (!item.getListedBy().equals(userId)) {
            throw new Exception("You are not authorized to upload images for this item.");
        }

        if (imageFiles != null && imageFiles.length > 0) {
            String itemImageDirPath = imageUploadDir + item.getRandomId() + "/";
            Path itemImageDirAbsolutePath = Paths.get(itemImageDirPath).toAbsolutePath();
            File itemImageDir = itemImageDirAbsolutePath.toFile();

            // Create directory if it doesn't exist
            if (!itemImageDir.exists() && !itemImageDir.mkdirs()) {
                throw new Exception("Failed to create image upload directory.");
            }

            // Determine the starting index
            int existingImageCount = item.getImageUrls().size();
            File[] existingFiles = itemImageDir.listFiles((dir, name) -> name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg"));
            if (existingFiles != null) {
                existingImageCount = existingFiles.length;
            }

            List<String> uploadedImageUrls = new ArrayList<>();

            for (int i = 0; i < imageFiles.length; i++) {
                MultipartFile imageFile = imageFiles[i];
                if (!imageFile.isEmpty()) {
                    String originalFilename = imageFile.getOriginalFilename();
                    String extension = (originalFilename != null && originalFilename.contains("."))
                            ? originalFilename.substring(originalFilename.lastIndexOf("."))
                            : ".png"; // Default to .png

                    // Generate new image name
                    String newImageName = item.getRandomId() + "-" + (existingImageCount + i + 1) + extension;
                    Path imagePath = itemImageDirAbsolutePath.resolve(newImageName);
                    File dest = imagePath.toFile();

                    // Save image
                    try {
                        BufferedImage bufferedImage = ImageIO.read(imageFile.getInputStream());
                        if (bufferedImage == null) {
                            throw new Exception("Invalid image file: " + originalFilename);
                        }
                        ImageIO.write(bufferedImage, "png", dest);
                        logger.info("Saved image as PNG: {}", dest.getAbsolutePath());
                    } catch (IOException e) {
                        logger.error("Failed to save image: {}", dest.getAbsolutePath(), e);
                        throw new Exception("Error saving image: " + originalFilename);
                    }

                    // Construct image URL
                    String imageUrl = "/itemimages/" + item.getRandomId() + "/" + newImageName;

                    // Add to item
                    item.addImageUrl(imageUrl);
                    uploadedImageUrls.add(imageUrl);
                }
            }

            // Save updated item
            saveItem(item);
            logger.info("Uploaded images for item ID: {}", id);

            return uploadedImageUrls;
        }

        throw new Exception("No images to upload.");
    }
}
