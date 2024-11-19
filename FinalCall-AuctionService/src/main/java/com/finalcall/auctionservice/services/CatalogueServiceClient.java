/**
 * Feign client interface for communicating with the external Catalogue Service.
 * Allows the Auction Service to retrieve and update item details from the Catalogue Service.
 */
package com.finalcall.auctionservice.services;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import com.finalcall.auctionservice.dto.ItemDTO;

@FeignClient(name = "catalogue-service", url = "${catalogue.service.url}")
public interface CatalogueServiceClient {
    @GetMapping("/catalogue/items")
    List<ItemDTO> getAllItems();

    @GetMapping("/catalogue/items/{id}")
    ItemDTO getItemById(@PathVariable("id") Long id);

    @PutMapping("/catalogue/items/{id}")
    void updateItem(@PathVariable("id") Long id, @RequestBody ItemDTO item);

}