// src/main/java/com/finalcall/catalogueservice/service/ItemService.java

package com.finalcall.catalogueservice.service;

import com.finalcall.catalogueservice.entity.Item;
import com.finalcall.catalogueservice.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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

    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    public Optional<Item> getItemById(Long id) {
        return itemRepository.findById(id);
    }
    
    public List<Item> getItemsByUser(Long listedBy) {
        return itemRepository.findByListedBy(listedBy);
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
}
