package com.finalcall.auctionservice.event;

import com.finalcall.auctionservice.dto.AuctionDTO;
import org.springframework.context.ApplicationEvent;

/**
 * Event indicating that an auction has been updated.
 */
public class AuctionUpdatedEvent extends ApplicationEvent {
    
    private final Long auctionId;
    private final AuctionDTO auctionDTO;

    public AuctionUpdatedEvent(Object source, Long auctionId, AuctionDTO auctionDTO) {
        super(source);
        this.auctionId = auctionId;
        this.auctionDTO = auctionDTO;
    }

    public Long getAuctionId() {
        return auctionId;
    }

    public AuctionDTO getAuctionDTO() {
        return auctionDTO;
    }
}
