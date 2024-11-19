package com.finalcall.auctionservice.services;

import com.finalcall.auctionservice.dto.ItemDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "catalogue-service", url = "${catalogue.service.url}")
public interface CatalogueServiceClient {
    @GetMapping("/api/items/{id}")
    ItemDTO getItemById(@PathVariable("id") Long id);

    @PutMapping("/api/items/{id}")
    void updateItem(@PathVariable("id") Long id, @RequestBody ItemDTO item);
}