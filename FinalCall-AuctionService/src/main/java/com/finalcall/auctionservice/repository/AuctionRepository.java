package com.finalcall.auctionservice.repository;

import com.finalcall.auctionservice.entity.Auction;
import com.finalcall.auctionservice.entity.AuctionStatus;
import com.finalcall.auctionservice.entity.AuctionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AuctionRepository extends JpaRepository<Auction, Long> {
    List<Auction> findByAuctionTypeAndStatus(AuctionType auctionType, AuctionStatus status);
    Optional<Auction> findByItemId(Long itemId);
}
