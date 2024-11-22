package com.finalcall.auctionservice.repository;

import com.finalcall.auctionservice.entity.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository interface for Bid entity.
 */
@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {
    /**
     * Find bids by auction ID ordered by timestamp descending.
     *
     * @param auctionId The ID of the auction.
     * @return List of bids.
     */
    List<Bid> findByAuctionIdOrderByTimestampDesc(Long auctionId);

    /**
     * Find all bids by bidder ID.
     *
     * @param bidderId The ID of the bidder.
     * @return List of bids.
     */
    List<Bid> findAllByBidderId(Long bidderId);
}
