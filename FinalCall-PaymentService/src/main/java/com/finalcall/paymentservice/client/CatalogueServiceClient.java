//package com.finalcall.paymentservice.client;
//
//import org.springframework.cloud.openfeign.FeignClient;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import com.finalcall.catalogueservice.dto.ItemDTO;
//
//@FeignClient(name = "catalogue-service", url = "${catalogue.service.url}")
//public interface CatalogueServiceClient {
//    @GetMapping("/api/items/{itemId}")
//    ItemDTO getItemById(@PathVariable("itemId") Long itemId);
//}