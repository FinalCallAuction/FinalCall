//src/main/java/com/finalcall/paymentservice/client/AuctionServiceClient.java
package com.finalcall.paymentservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.finalcall.paymentservice.dto.AuctionDTO;

@FeignClient(name = "auction-service", url = "${auction.service.url}")
public interface AuctionServiceClient {
 @GetMapping("/api/auctions/{auctionId}")
 AuctionDTO getAuctionById(@PathVariable("auctionId") Long auctionId);
}

