// src/main/java/com/finalcall/catalogueservice/client/AuctionServiceClient.java

package com.finalcall.catalogueservice.client;

import com.finalcall.catalogueservice.config.FeignConfig;
import com.finalcall.catalogueservice.dto.AuctionDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Feign client to communicate with the AuctionService.
 */
@FeignClient(name = "auction-service", url = "${auction.service.url}", configuration = FeignConfig.class)
public interface AuctionServiceClient {

    /**
     * Create a new auction.
     *
     * @param auctionDTO Details of the auction to create.
     * @return ResponseEntity with the created AuctionDTO.
     */
    @PostMapping("/api/auctions/create")
    ResponseEntity<AuctionDTO> createAuction(@RequestBody AuctionDTO auctionDTO);

    /**
     * Get auction details by item ID.
     *
     * @param itemId ID of the item.
     * @return ResponseEntity containing AuctionDTO.
     */
    @GetMapping("/api/auctions/item/{itemId}")
    ResponseEntity<AuctionDTO> getAuctionByItemId(@PathVariable("itemId") Long itemId);
}
