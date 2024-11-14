package com.finalcall.backend.repository;

import com.finalcall.backend.entity.Item;
import com.finalcall.backend.entity.AuctionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByAuctionEndTimeAfter(LocalDateTime now);
    List<Item> findByAuctionType(AuctionType auctionType);
    
    // New method to check existence of randomId
    boolean existsByRandomId(String randomId);
}