// src/main/java/com/finalcall/backend/controller/ItemController.java

package com.finalcall.backend.controller;

import com.finalcall.backend.entity.Item;
import com.finalcall.backend.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@RestController
@RequestMapping("/api/items")
@CrossOrigin(origins = "http://localhost:3000")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @GetMapping("/{itemId}")
    public ResponseEntity<?> getItemDetails(@PathVariable Long itemId) {
        Optional<Item> itemOpt = itemService.getItemById(itemId);
        if (itemOpt.isPresent()) {
            return ResponseEntity.ok(itemOpt.get());
        } else {
            return ResponseEntity.badRequest().body("Item not found");
        }
    }

    // Additional endpoints for item management can be added here
}
