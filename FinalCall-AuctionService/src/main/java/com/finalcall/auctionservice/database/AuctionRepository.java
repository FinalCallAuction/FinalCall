/**
 * Data access layer for the Auction entity.
 * Extends JpaRepository to provide CRUD operations on auctions.
 * Defines custom query methods based on auction attributes.
 */

package com.finalcall.auctionservice.database;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.finalcall.auctionservice.model.Auction;
import com.finalcall.auctionservice.model.Auction.AuctionStatus;
import com.finalcall.auctionservice.model.Auction.AuctionType;

public interface AuctionRepository extends JpaRepository<Auction, Long> {
    List<Auction> findByStatus(AuctionStatus status);

    List<Auction> findByType(AuctionType type);

    Optional<Auction> findByItemId(Long itemId);

}