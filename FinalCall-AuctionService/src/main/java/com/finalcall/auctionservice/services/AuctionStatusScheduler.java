/**
 * Scheduled task that periodically updates the status of auctions based on the current time and bids.
 * Ensures auctions transition from ACTIVE to ENDED, EXPIRED, or AWAITING_PAYMENT appropriately.
 */
package com.finalcall.auctionservice.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.scheduling.annotation.Scheduled;

import com.finalcall.auctionservice.database.AuctionRepository;
import com.finalcall.auctionservice.database.BidRepository;
import com.finalcall.auctionservice.dto.ItemDTO;
import com.finalcall.auctionservice.model.Auction;
import com.finalcall.auctionservice.model.Auction.AuctionStatus;
import com.finalcall.auctionservice.model.Auction.AuctionType;

import org.springframework.stereotype.Component;

@Component
public class AuctionStatusScheduler {
    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final CatalogueServiceClient catalogueServiceClient;

    public AuctionStatusScheduler(AuctionRepository auctionRepository, BidRepository bidRepository,
                                  CatalogueServiceClient catalogueServiceClient) {
        this.auctionRepository = auctionRepository;
        this.bidRepository = bidRepository;
        this.catalogueServiceClient = catalogueServiceClient;

    }

    @Scheduled(fixedRate = 100000)
    public void updateAuctionStatuses() {
        List<Auction> activeAuctions = auctionRepository.findByStatus(AuctionStatus.ACTIVE);
        List<Auction> nullAuctions = auctionRepository.findByStatus(null);
        activeAuctions.addAll(nullAuctions);
        LocalDateTime now = LocalDateTime.now();

        for (Auction auction : activeAuctions) {
            try {

                if (auction.getEndTime().isBefore(now)) {
                    if (bidRepository.findByAuctionId(auction.getId()).isEmpty()) {
                        auction.setStatus(Auction.AuctionStatus.EXPIRED);
                    } else {
                        auction.setStatus(Auction.AuctionStatus.AWAITING_PAYMENT);
                    }
                    auctionRepository.save(auction);
                }
            } catch (Exception e) {
                // Log the error or handle it accordingly
                // Optionally, you can continue with the next iteration
            }
        }
    }

}