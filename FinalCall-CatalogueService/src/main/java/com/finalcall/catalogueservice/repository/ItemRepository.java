package com.finalcall.catalogueservice.repository;

import com.finalcall.catalogueservice.entity.AuctionType;
import com.finalcall.catalogueservice.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByAuctionEndTimeAfterAndSoldFalse(LocalDateTime now);
    List<Item> findByAuctionTypeAndSoldFalse(AuctionType auctionType);
    boolean existsByRandomId(String randomId);
    List<Item> findByListedByAndAuctionEndTimeAfterAndSoldFalse(String listedBy, LocalDateTime now);
}
