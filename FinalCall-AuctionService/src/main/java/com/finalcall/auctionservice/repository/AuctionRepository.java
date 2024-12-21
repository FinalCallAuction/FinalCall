// src/main/java/com/finalcall/auctionservice/repository/AuctionRepository.java

package com.finalcall.auctionservice.repository;

import com.finalcall.auctionservice.entity.Auction;
import com.finalcall.auctionservice.entity.AuctionStatus;
import com.finalcall.auctionservice.entity.AuctionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {
    Optional<Auction> findByItemId(Long itemId);
    List<Auction> findByAuctionTypeAndStatus(AuctionType auctionType, AuctionStatus status);
    List<Auction> findByStatus(AuctionStatus status);
}
