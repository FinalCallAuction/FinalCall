package com.finalcall.catalogueservice.service;

import com.finalcall.catalogueservice.entity.Item;
import com.finalcall.catalogueservice.entity.AuctionType;
import com.finalcall.catalogueservice.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class ItemService {

    @Autowired
    private ItemRepository itemRepository;

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

    public List<Item> getAllActiveItems() {
        return itemRepository.findByAuctionEndTimeAfterAndSoldFalse(LocalDateTime.now());
    }

    public List<Item> getItemsByAuctionType(AuctionType auctionType) {
        return itemRepository.findByAuctionTypeAndSoldFalse(auctionType);
    }

    public Optional<Item> getItemById(Long id) {
        return itemRepository.findById(id);
    }
    
    public List<Item> getActiveListingsByUser(String listedBy) {
        return itemRepository.findByListedByAndAuctionEndTimeAfterAndSoldFalse(listedBy, LocalDateTime.now());
    }

    public Item createItem(Item item) {
        String randomId;
        do {
            randomId = generateRandomId();
        } while (itemRepository.existsByRandomId(randomId));
        item.setRandomId(randomId);
        item.setCurrentBid(item.getStartingBid());
        item.setSold(false);
        return itemRepository.save(item);
    }

    public Item saveItem(Item item) {
        return itemRepository.save(item);
    }

    // Place a new bid
    public Item placeBid(Long itemId, BigDecimal bidAmount, Long bidderId) throws Exception {
        Optional<Item> itemOpt = itemRepository.findById(itemId);
        if (!itemOpt.isPresent()) {
            throw new Exception("Item not found.");
        }
        Item item = itemOpt.get();
        if (item.isSold()) {
            throw new Exception("Auction already sold.");
        }
        if (bidAmount.compareTo(item.getCurrentBid()) <= 0) {
            throw new Exception("Bid must be higher than current bid.");
        }
        item.setCurrentBid(bidAmount);
        // Optionally, track bidder information (not implemented here)
        return itemRepository.save(item);
    }

    // Accept the current bid by the lister
    public void acceptBid(Long itemId, String username) throws Exception {
        Optional<Item> itemOpt = itemRepository.findById(itemId);
        if (!itemOpt.isPresent()) {
            throw new Exception("Item not found.");
        }
        Item item = itemOpt.get();
        if (!item.getListedBy().equals(username)) {
            throw new Exception("You are not authorized to accept bids for this item.");
        }
        if (item.getCurrentBid().compareTo(item.getStartingBid()) <= 0) {
            throw new Exception("No valid bids to accept.");
        }
        // Finalize the auction
        item.setAuctionEndTime(LocalDateTime.now());
        item.setSold(true);
        itemRepository.save(item);
    }
}
