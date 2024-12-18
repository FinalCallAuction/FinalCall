package com.finalcall.auctionservice.service;

import com.finalcall.auctionservice.dto.ItemDTO;
import com.finalcall.auctionservice.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ItemService {
    @Autowired
    private MicroserviceWebSocketService webSocketService;

    public ItemDTO getItemById(Long itemId) {
        return webSocketService.sendRequest(
            "catalogue",
            "GET_ITEM",
            itemId,
            ItemDTO.class
        ).join();
    }

    public UserDTO getUserById(Long userId) {
        return webSocketService.sendRequest(
            "auth",
            "user.getById",
            userId,
            UserDTO.class
        ).join();
    }

}