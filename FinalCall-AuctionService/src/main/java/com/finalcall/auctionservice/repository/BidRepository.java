// src/main/java/com/finalcall/auctionservice/database/BidRepository.java

package com.finalcall.auctionservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.finalcall.auctionservice.entity.Bid;

public interface BidRepository extends JpaRepository<Bid, Long> {
    List<Bid> findByAuctionId(Long auctionId);
    Optional<Bid> findTopByAuctionIdOrderByAmountDesc(Long auctionId);
    List<Bid> findAllByBidderId(Long bidderId);
}
