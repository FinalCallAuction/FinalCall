// src/main/java/com/finalcall/backend/service/ItemService.java

package com.finalcall.backend.service;

import com.finalcall.backend.entity.Item;
import com.finalcall.backend.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ItemService {

    @Autowired
    private ItemRepository itemRepository;

    public Optional<Item> getItemById(Long id) {
        return itemRepository.findById(id);
    }

    // Additional methods for creating, updating, and deleting items can be added
}
