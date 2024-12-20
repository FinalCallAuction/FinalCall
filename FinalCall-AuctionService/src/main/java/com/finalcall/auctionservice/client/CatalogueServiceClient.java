package com.finalcall.auctionservice.client;

import com.finalcall.auctionservice.dto.ItemDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "catalogue-service", url = "${catalogue.service.url}")
public interface CatalogueServiceClient {
    @GetMapping("/api/items/{itemId}")
    ItemDTO getItemById(@PathVariable("itemId") Long itemId);
}