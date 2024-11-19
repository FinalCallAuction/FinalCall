/**
 * Data access layer for the Bid entity.
 * Extends JpaRepository to provide CRUD operations on bids.
 * Defines custom query methods based on bid attributes.
 */

package com.finalcall.auctionservice.database;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.finalcall.auctionservice.model.Bid;

public interface BidRepository extends JpaRepository<Bid, Long> {
    List<Bid> findByAuctionId(Long auctionId);

    Optional<Bid> findTopByAuctionIdOrderByAmountDesc(Long auctionId);

    List<Bid> findAllByBidderId(Long bidderId);

}