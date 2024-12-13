// src/main/java/com/finalcall/auctionservice/repository/BidRepository.java

package com.finalcall.auctionservice.repository;

import com.finalcall.auctionservice.entity.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {
    List<Bid> findByAuctionIdOrderByTimestampDesc(Long auctionId);
    List<Bid> findAllByBidderId(Long bidderId);
}
