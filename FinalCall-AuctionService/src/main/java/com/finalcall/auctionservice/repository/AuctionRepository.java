// src/main/java/com/finalcall/auctionservice/database/AuctionRepository.java

package com.finalcall.auctionservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finalcall.auctionservice.entity.Auction;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Auction entities.
 */
@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {
    /**
     * Finds an auction by its associated catalogue item ID.
     *
     * @param catalogueItemId ID of the catalogue item.
     * @return Optional containing the found auction or empty if not found.
     */
    Optional<Auction> findByCatalogueItemId(Long catalogueItemId);

    /**
     * Finds all auctions associated with a list of catalogue item IDs.
     *
     * @param catalogueItemIds List of catalogue item IDs.
     * @return List of corresponding auctions.
     */
    List<Auction> findAllByCatalogueItemIdIn(List<Long> catalogueItemIds);
}
