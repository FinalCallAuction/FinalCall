// src/main/java/com/finalcall/backend/service/ItemService.java

package com.finalcall.backend.service;

import com.finalcall.backend.entity.Item;
import com.finalcall.backend.entity.AuctionType;
import com.finalcall.backend.entity.User;
import com.finalcall.backend.repository.ItemRepository;
import com.finalcall.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class ItemService {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int RANDOM_ID_LENGTH = 8;
    private Random random = new Random();

    // Method to generate unique 8-character randomId
    private String generateRandomId() {
        StringBuilder sb = new StringBuilder(RANDOM_ID_LENGTH);
        for(int i =0; i<RANDOM_ID_LENGTH; i++) {
            sb.append(ALPHANUMERIC.charAt(random.nextInt(ALPHANUMERIC.length())));
        }
        return sb.toString();
    }

    // List all active items
    public List<Item> getAllActiveItems() {
        return itemRepository.findByAuctionEndTimeAfter(LocalDateTime.now());
    }

    // List items by auction type
    public List<Item> getItemsByAuctionType(AuctionType auctionType) {
        return itemRepository.findByAuctionType(auctionType);
    }

    // Get item by ID
    public Optional<Item> getItemById(Long id) {
        return itemRepository.findById(id);
    }

    // Create a new item with randomId generation
    public Item createItem(String name, Double startingBid, AuctionType auctionType, LocalDateTime auctionEndTime, Long userId, String imageUrl) throws Exception {
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new Exception("User not found");
        }

        User user = userOpt.get();

        // Generate unique randomId
        String randomId;
        do {
            randomId = generateRandomId();
        } while(itemRepository.existsByRandomId(randomId));

        Item item = new Item(name, startingBid, auctionType, auctionEndTime, user, imageUrl);
        item.setRandomId(randomId);
        item.setCurrentBid(startingBid); // Ensure currentBid is set

        return itemRepository.save(item);
    }

    // Update current bid
    public Item updateCurrentBid(Long itemId, Double newBid) throws Exception {
        Optional<Item> itemOpt = itemRepository.findById(itemId);
        if (!itemOpt.isPresent()) {
            throw new Exception("Item not found");
        }

        Item item = itemOpt.get();
        item.setCurrentBid(newBid);
        return itemRepository.save(item);
    }

    // Save item (used for updating auction end time)
    public Item saveItem(Item item) {
        return itemRepository.save(item);
    }

    // Additional methods for auction logic can be added here
}
