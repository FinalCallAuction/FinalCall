package com.finalcall.auctionservice.event;

import com.finalcall.auctionservice.dto.AuctionDTO;
import com.finalcall.auctionservice.websocket.AuctionWSHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AuctionUpdatedEventListener {

    @Autowired
    private AuctionWSHandler auctionWSHandler;

    @EventListener
    public void handleAuctionUpdatedEvent(AuctionUpdatedEvent event) throws IOException {
        Long auctionId = event.getAuctionId();
        AuctionDTO auctionDTO = event.getAuctionDTO();
        auctionWSHandler.broadcast(auctionId.toString(), auctionDTO);
    }
}
