// src/main/java/com/finalcall/auctionservice/services/AuctionService.java

package com.finalcall.auctionservice.services;

import com.finalcall.auctionservice.dto.AuctionDTO;
import com.finalcall.auctionservice.entity.Auction;
import com.finalcall.auctionservice.entity.AuctionType;
import com.finalcall.auctionservice.repository.AuctionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class containing business logic for Auctions.
 */
@Service
public class AuctionService {

    @Autowired
    private AuctionRepository auctionRepository;

    /**
     * Creates a new auction based on the provided AuctionDTO.
     *
     * @param auctionDTO Data Transfer Object containing auction details.
     * @return The created AuctionDTO.
     * @throws Exception if an auction for the given catalogue item ID already exists.
     */
    public AuctionDTO createAuction(AuctionDTO auctionDTO, Jwt principal) throws Exception {
        Long sellerId = principal.getClaim("id");
        if (sellerId == null) {
            throw new Exception("Invalid token: missing user ID.");
        }

        if (auctionRepository.findByCatalogueItemId(auctionDTO.getCatalogueItemId()).isPresent()) {
            throw new Exception("Auction for this item already exists.");
        }

        Auction auction = new Auction();
        auction.setCatalogueItemId(auctionDTO.getCatalogueItemId());
        auction.setAuctionType(AuctionType.valueOf(auctionDTO.getAuctionType()));
        auction.setStartingBidPrice(auctionDTO.getStartingBidPrice());
        auction.setCurrentBidPrice(auctionDTO.getStartingBidPrice());
        auction.setAuctionEndTime(auctionDTO.getAuctionEndTime());
        auction.setSellerId(sellerId);
        auction.setStartTime(LocalDateTime.now());

        Auction savedAuction = auctionRepository.save(auction);

        AuctionDTO savedAuctionDTO = new AuctionDTO();
        savedAuctionDTO.setCatalogueItemId(savedAuction.getCatalogueItemId());
        savedAuctionDTO.setAuctionType(savedAuction.getAuctionType().name());
        savedAuctionDTO.setStartingBidPrice(savedAuction.getStartingBidPrice());
        savedAuctionDTO.setAuctionEndTime(savedAuction.getAuctionEndTime());
        savedAuctionDTO.setSellerId(savedAuction.getSellerId());

        return savedAuctionDTO;
    }

    /**
     * Retrieves all auctions corresponding to a list of catalogue item IDs.
     *
     * @param itemIds List of catalogue item IDs.
     * @return List of AuctionDTOs.
     */
    public List<AuctionDTO> getAuctionsByCatalogueItemIds(List<Long> itemIds) {
        List<Auction> auctions = auctionRepository.findAllByCatalogueItemIdIn(itemIds);
        return auctions.stream().map(auction -> {
            AuctionDTO dto = new AuctionDTO();
            dto.setCatalogueItemId(auction.getCatalogueItemId());
            dto.setAuctionType(auction.getAuctionType().name());
            dto.setStartingBidPrice(auction.getStartingBidPrice());
            dto.setAuctionEndTime(auction.getAuctionEndTime());
            dto.setSellerId(auction.getSellerId());
            dto.setStartTime(auction.getStartTime());
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * Parses the auction type string to the corresponding AuctionType enum.
     *
     * @param auctionTypeStr The auction type as a string.
     * @return The corresponding AuctionType enum.
     * @throws IllegalArgumentException if the auction type is invalid.
     */
    private AuctionType parseAuctionType(String auctionTypeStr) {
        try {
            return AuctionType.valueOf(auctionTypeStr.toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid auction type: " + auctionTypeStr);
        }
    }

    // Additional business logic methods can be added here
}
