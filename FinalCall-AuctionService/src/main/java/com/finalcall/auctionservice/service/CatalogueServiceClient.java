// src/main/java/com/finalcall/auctionservice/services/CatalogueServiceClient.java

package com.finalcall.auctionservice.service;

import com.finalcall.auctionservice.dto.AuctionDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "catalogue-service", url = "${catalogue.service.url}")
public interface CatalogueServiceClient {

    @GetMapping("/api/auctions/{id}")
    AuctionDTO getAuctionById(@PathVariable("id") Long id);

    @PutMapping("/api/auctions/{id}")
    void updateAuction(@PathVariable("id") Long id, @RequestBody AuctionDTO auctionDTO);
}
