package com.finalcall.auctionservice.repository;

import com.finalcall.auctionservice.entity.Auction;
import com.finalcall.auctionservice.entity.AuctionStatus;
import com.finalcall.auctionservice.entity.AuctionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Auction entity.
 */
public interface AuctionRepository extends JpaRepository<Auction, Long> {
    /**
     * Find auctions by type and status.
     *
     * @param auctionType The type of auction.
     * @param status      The status of the auction.
     * @return List of matching auctions.
     */
    List<Auction> findByAuctionTypeAndStatus(AuctionType auctionType, AuctionStatus status);

    /**
     * Find an auction by item ID.
     *
     * @param itemId The ID of the item.
     * @return Optional containing the Auction if found.
     */
    Optional<Auction> findByItemId(Long itemId);
}
