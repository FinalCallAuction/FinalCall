package com.finalcall.catalogueservice.service;

import com.finalcall.catalogueservice.dto.AuctionDTO;
import com.finalcall.catalogueservice.dto.ItemDTO;
import com.finalcall.catalogueservice.dto.ItemRequest;
import com.finalcall.catalogueservice.dto.UserDTO;
import com.finalcall.catalogueservice.entity.Item;
import com.finalcall.catalogueservice.exception.UserNotFoundException;
import com.finalcall.catalogueservice.repository.ItemRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class ItemService {
    private static final Logger logger = LoggerFactory.getLogger(ItemService.class);

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private WebSocketCommunicationService webSocketService;

    @Value("${image.upload.dir}")
    private String imageUploadDir;

    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWxYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int RANDOM_ID_LENGTH = 8;
    private final Random random = new Random();

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

    private ItemDTO mapToItemDTO(Item item, AuctionDTO auctionDTO, String sellerName) {
        logger.debug("Mapping item {} to DTO with auction data: {}",
            item.getId(), auctionDTO != null ? "present" : "absent");

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

    @Transactional
    public ItemDTO createItemWithAuction(ItemRequest itemRequest, Long userId) throws Exception {
        // Create and save item
        Item item = new Item();
        item.setName(itemRequest.getName());
        item.setDescription(itemRequest.getDescription());
        item.setListedBy(userId);
        item.setStartingBidPrice(itemRequest.getStartingBid());
        Item savedItem = createItem(item);

        // Create AuctionDTO
        AuctionDTO auctionDTO = new AuctionDTO();
        auctionDTO.setItemId(savedItem.getId());
        auctionDTO.setAuctionType(itemRequest.getAuctionType());
        auctionDTO.setStartingBidPrice(itemRequest.getStartingBid());
        auctionDTO.setCurrentBidPrice(itemRequest.getStartingBid());
        auctionDTO.setAuctionEndTime(itemRequest.getAuctionEndTime());
        auctionDTO.setSellerId(userId);
        auctionDTO.setStartTime(itemRequest.getAuctionStartTime() != null ?
            itemRequest.getAuctionStartTime() : LocalDateTime.now());

        if ("DUTCH".equalsIgnoreCase(itemRequest.getAuctionType())) {
            auctionDTO.setPriceDecrement(itemRequest.getPriceDecrement());
            auctionDTO.setMinimumPrice(itemRequest.getMinimumPrice());
        }

        // Register synchronization to create auction after transaction commits
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    CompletableFuture<AuctionDTO> auctionFuture = webSocketService.sendRequest(
                        "auction",
                        "auction.create",
                        auctionDTO,
                        AuctionDTO.class
                    );
                    AuctionDTO createdAuction = auctionFuture.get(5, TimeUnit.SECONDS);
                    if (createdAuction == null) {
                        logger.error("No auction returned for item: {}", savedItem.getId());
                    } else {
                        logger.info("Auction created successfully for item: {}", savedItem.getId());
                    }
                } catch (Exception e) {
                    logger.error("Error creating auction after commit for item {}", savedItem.getId(), e);
                }
            }
        });

        String sellerName = fetchSellerName(userId);
        return mapToItemDTO(savedItem, auctionDTO, sellerName);
    }

    private String fetchSellerName(Long userId) {
        try {
            CompletableFuture<UserDTO> userFuture = webSocketService.sendRequest(
                "auth",
                "user.getById",
                userId,
                UserDTO.class
            );

            UserDTO userDTO = userFuture.get(5, TimeUnit.SECONDS);
            if (userDTO != null && userDTO.getUsername() != null) {
                return userDTO.getUsername();
            }
        } catch (UserNotFoundException e) {
            logger.warn("User not found with ID: {}", userId);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error fetching user with ID: {}", userId, e);
        }
        return "Unknown";
    }

    public List<ItemDTO> getAllItemsWithDetails() {
        try {
            List<Item> items = getAllItems();
            logger.info("Fetched {} items from repository", items.size());

            List<ItemDTO> itemDTOs = new ArrayList<>();

            for (Item item : items) {
                // Initialize images
                item.getImageUrls().size();

                try {
                    logger.info("Processing item ID: {} - Starting auction data fetch", item.getId());
                    logger.info("WebSocket connection status for auction service: {}",
                        webSocketService.isConnected("auction") ? "Connected" : "Disconnected");

                    CompletableFuture<AuctionDTO> auctionFuture = webSocketService.sendRequest(
                        "auction",
                        "auction.getByItemId",
                        item.getId(),
                        AuctionDTO.class
                    );

                    try {
                        AuctionDTO auctionDTO = auctionFuture.get(5, TimeUnit.SECONDS);
                        logger.info("Item ID: {} - Auction data received: {}",
                            item.getId(),
                            auctionDTO != null ? "Success" : "Null");
                        if (auctionDTO != null) {
                            logger.info("Auction details for Item {}: Type={}, Status={}, Current Price={}",
                                item.getId(),
                                auctionDTO.getAuctionType(),
                                auctionDTO.getStatus(),
                                auctionDTO.getCurrentBidPrice());
                        }

                        String sellerName = fetchSellerName(item.getListedBy());
                        ItemDTO itemDTO = mapToItemDTO(item, auctionDTO, sellerName);
                        itemDTOs.add(itemDTO);
                    } catch (TimeoutException e) {
                        logger.error("Timeout waiting for auction data for item {}", item.getId(), e);
                        ItemDTO itemDTO = mapToItemDTO(item, null, fetchSellerName(item.getListedBy()));
                        itemDTOs.add(itemDTO);
                    }
                } catch (Exception e) {
                    logger.error("Error processing item {}: {}", item.getId(), e.getMessage());
                    logger.error("Full error details:", e);
                    ItemDTO itemDTO = mapToItemDTO(item, null, fetchSellerName(item.getListedBy()));
                    itemDTOs.add(itemDTO);
                }
            }

            return itemDTOs;
        } catch (Exception e) {
            logger.error("Unexpected error in getAllItemsWithDetails", e);
            throw new RuntimeException("Failed to retrieve items", e);
        }
    }

    public ItemDTO getItemDetails(Long id) throws Exception {
        Optional<Item> itemOpt = getItemById(id);
        if (itemOpt.isEmpty()) {
            throw new Exception("Item not found.");
        }

        Item item = itemOpt.get();
        // Initialize images
        item.getImageUrls().size();

        CompletableFuture<AuctionDTO> auctionFuture = webSocketService.sendRequest(
            "auction",
            "auction.getByItemId",
            item.getId(),
            AuctionDTO.class
        );

        AuctionDTO auctionDTO = auctionFuture.get(5, TimeUnit.SECONDS);
        String sellerName = fetchSellerName(item.getListedBy());

        return mapToItemDTO(item, auctionDTO, sellerName);
    }

    public List<ItemDTO> getUserItemsWithDetails(Long userId) {
        List<Item> items = getItemsByUser(userId);
        List<ItemDTO> itemDTOs = new ArrayList<>();

        for (Item item : items) {
            try {
                CompletableFuture<AuctionDTO> auctionFuture = webSocketService.sendRequest(
                    "auction",
                    "auction.getByItemId",
                    item.getId(),
                    AuctionDTO.class
                );

                AuctionDTO auctionDTO = auctionFuture.get(5, TimeUnit.SECONDS);
                String sellerName = fetchSellerName(item.getListedBy());
                ItemDTO itemDTO = mapToItemDTO(item, auctionDTO, sellerName);
                itemDTOs.add(itemDTO);
            } catch (Exception e) {
                logger.error("Error fetching details for user item ID: {}", item.getId(), e);
            }
        }

        return itemDTOs;
    }

    @Transactional
    public ItemDTO getItemWithoutAuctionDetails(Long id) throws Exception {
        Optional<Item> itemOpt = getItemById(id);
        if (itemOpt.isEmpty()) {
            throw new Exception("Item not found.");
        }

        Item item = itemOpt.get();
        // Force initialize lazy collection
        item.getImageUrls().size();

        String sellerName = fetchSellerName(item.getListedBy());
        return mapToItemDTO(item, null, sellerName);
    }


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

        if (imageFiles == null || imageFiles.length == 0) {
            throw new Exception("No images to upload.");
        }

        String itemImageDirPath = imageUploadDir + item.getRandomId() + "/";
        Path itemImageDirAbsolutePath = Paths.get(itemImageDirPath).toAbsolutePath();
        File itemImageDir = itemImageDirAbsolutePath.toFile();

        if (!itemImageDir.exists() && !itemImageDir.mkdirs()) {
            throw new Exception("Failed to create image upload directory.");
        }

        int existingImageCount = item.getImageUrls().size();
        File[] existingFiles = itemImageDir.listFiles((dir, name) ->
            name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg"));
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
                        : ".png";

                String newImageName = item.getRandomId() + "-" + (existingImageCount + i + 1) + extension;
                Path imagePath = itemImageDirAbsolutePath.resolve(newImageName);
                File dest = imagePath.toFile();

                try {
                    BufferedImage bufferedImage = ImageIO.read(imageFile.getInputStream());
                    if (bufferedImage == null) {
                        throw new Exception("Invalid image file: " + originalFilename);
                    }
                    ImageIO.write(bufferedImage, "png", dest);
                    String imageUrl = "/itemimages/" + item.getRandomId() + "/" + newImageName;
                    item.addImageUrl(imageUrl);
                    uploadedImageUrls.add(imageUrl);
                    logger.info("Saved image: {}", dest.getAbsolutePath());
                } catch (IOException e) {
                    logger.error("Failed to save image: {}", dest.getAbsolutePath(), e);
                    throw new Exception("Error saving image: " + originalFilename);
                }
            }
        }

        saveItem(item);
        logger.info("Uploaded {} images for item ID: {}", uploadedImageUrls.size(), id);
        return uploadedImageUrls;
    }
}
