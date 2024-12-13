// src/main/java/com/finalcall/auctionservice/service/AuctionService.java

package com.finalcall.auctionservice.service;

import com.finalcall.auctionservice.dto.*;
import com.finalcall.auctionservice.entity.*;
import com.finalcall.auctionservice.event.AuctionUpdatedEvent;
import com.finalcall.auctionservice.repository.AuctionRepository;
import com.finalcall.auctionservice.repository.BidRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AuctionService {

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private BidRepository bidRepository;

    @Autowired
    private AuthenticationServiceClient authenticationServiceClient;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    /**
     * Creates a new auction based on the provided AuctionDTO.
     *
     * @param auctionDTO Details of the auction.
     * @return The created Auction entity.
     */
    @Transactional
    public Auction createAuction(AuctionDTO auctionDTO) {
        Auction auction = new Auction();
        auction.setItemId(auctionDTO.getItemId());
        auction.setAuctionType(auctionDTO.getAuctionType());
        auction.setStartingBidPrice(auctionDTO.getStartingBidPrice());

        // Initialize currentBidPrice based on auction type
        if (auctionDTO.getAuctionType() == AuctionType.FORWARD) {
            auction.setCurrentBidPrice(auctionDTO.getStartingBidPrice());
        } else if (auctionDTO.getAuctionType() == AuctionType.DUTCH) {
            auction.setCurrentBidPrice(auctionDTO.getStartingBidPrice());

            // Set price decrement and minimum price for Dutch auctions
            auction.setPriceDecrement(auctionDTO.getPriceDecrement());
            auction.setMinimumPrice(auctionDTO.getMinimumPrice());
        }

        auction.setAuctionEndTime(auctionDTO.getAuctionEndTime());
        auction.setSellerId(auctionDTO.getSellerId());
        auction.setStartTime(auctionDTO.getStartTime());
        auction.setStatus(AuctionStatus.ACTIVE);

        // Save auction
        Auction savedAuction = auctionRepository.save(auction);

        // Publish the event
        eventPublisher.publishEvent(new AuctionUpdatedEvent(this, savedAuction));

        return savedAuction;
    }

    /**
     * Finds an auction by its associated item ID.
     *
     * @param itemId The ID of the item.
     * @return Optional containing the Auction if found.
     */
    public Optional<Auction> findByItemId(Long itemId) {
        return auctionRepository.findByItemId(itemId);
    }

    /**
     * Places a bid on an auction.
     *
     * @param auctionId  The ID of the auction.
     * @param bidRequest The bid details.
     * @return BidResponse indicating the result.
     */
    @Transactional
    public BidResponse placeBid(Long auctionId, BidRequest bidRequest) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new NoSuchElementException("Auction not found with ID: " + auctionId));

        // Check if auction is active
        if (auction.getStatus() != AuctionStatus.ACTIVE) {
            throw new IllegalStateException("Auction is not active.");
        }

        if (auction.getAuctionType() == AuctionType.FORWARD) {
            return handleForwardAuctionBid(auction, bidRequest);
        } else if (auction.getAuctionType() == AuctionType.DUTCH) {
            return handleDutchAuctionBid(auction, bidRequest);
        } else {
            throw new UnsupportedOperationException("Unsupported auction type.");
        }
    }

    /**
     * Handles bid placement for forward auctions.
     *
     * @param auction    The Auction entity.
     * @param bidRequest The bid details.
     * @return BidResponse indicating success.
     */
    private BidResponse handleForwardAuctionBid(Auction auction, BidRequest bidRequest) {
        if (bidRequest.getBidAmount() == null || bidRequest.getBidAmount() <= auction.getCurrentBidPrice()) {
            throw new IllegalArgumentException("Bid must be higher than the current bid.");
        }

        // Update auction's current bid
        auction.setCurrentBidPrice(bidRequest.getBidAmount());
        auction.setCurrentBidderId(bidRequest.getBidderId());
        auctionRepository.save(auction);

        // Save the bid
        Bid bid = new Bid(bidRequest.getBidAmount(), auction.getId(), bidRequest.getBidderId());
        bidRepository.save(bid);

        // Publish the event
        eventPublisher.publishEvent(new AuctionUpdatedEvent(this, auction));

        return new BidResponse("Bid placed successfully.", auction.getCurrentBidPrice());
    }

    /**
     * Handles bid placement for dutch auctions (buy now).
     *
     * @param auction    The Auction entity.
     * @param bidRequest The bid details.
     * @return BidResponse indicating success.
     */
    private BidResponse handleDutchAuctionBid(Auction auction, BidRequest bidRequest) {
        // In dutch auctions, first bid wins at current price
        auction.setStatus(AuctionStatus.ENDED);
        auction.setCurrentBidderId(bidRequest.getBidderId());
        auctionRepository.save(auction);

        // Save the bid
        Bid bid = new Bid(auction.getCurrentBidPrice(), auction.getId(), bidRequest.getBidderId());
        bidRepository.save(bid);

        // Publish the event
        eventPublisher.publishEvent(new AuctionUpdatedEvent(this, auction));

        return new BidResponse("Auction won at price: " + auction.getCurrentBidPrice(), auction.getCurrentBidPrice());
    }

    /**
     * Scheduled task to decrease prices in active dutch auctions every minute.
     */
    @Scheduled(fixedRate = 60000) // Every minute
    @Transactional
    public void decreaseDutchAuctionPrices() {
        List<Auction> activeDutchAuctions = auctionRepository.findByAuctionTypeAndStatus(AuctionType.DUTCH, AuctionStatus.ACTIVE);

        for (Auction auction : activeDutchAuctions) {
            double newPrice = auction.getCurrentBidPrice() - auction.getPriceDecrement();

            if (newPrice <= auction.getMinimumPrice()) {
                auction.setCurrentBidPrice(auction.getMinimumPrice());
                auction.setStatus(AuctionStatus.ENDED);
            } else {
                auction.setCurrentBidPrice(newPrice);
            }

            auctionRepository.save(auction);

            // Publish the event
            eventPublisher.publishEvent(new AuctionUpdatedEvent(this, auction));
        }
    }

    /**
     * Retrieves all bids for a specific auction and maps them to BidDTOs.
     *
     * @param auctionId The ID of the auction.
     * @return List of BidDTOs.
     */
    public List<BidDTO> getBidsForAuction(Long auctionId) {
        List<Bid> bids = bidRepository.findByAuctionIdOrderByTimestampDesc(auctionId);
        return bids.stream()
                .map(bid -> {
                    UserDTO user = authenticationServiceClient.getUserById(bid.getBidderId());
                    return new BidDTO(
                            bid.getId(),
                            bid.getAmount(),
                            bid.getBidderId(),
                            user != null ? user.getUsername() : "Unknown",
                            bid.getTimestamp()
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * Maps Auction entity to AuctionDTO.
     *
     * @param auction The Auction entity.
     * @return AuctionDTO object.
     */
    public AuctionDTO mapToDTO(Auction auction) {
        AuctionDTO dto = new AuctionDTO();
        dto.setId(auction.getId());
        dto.setItemId(auction.getItemId());
        dto.setAuctionType(auction.getAuctionType());
        dto.setStartingBidPrice(auction.getStartingBidPrice());
        dto.setCurrentBidPrice(auction.getCurrentBidPrice());
        dto.setAuctionEndTime(auction.getAuctionEndTime());
        dto.setSellerId(auction.getSellerId());
        dto.setStartTime(auction.getStartTime());
        dto.setPriceDecrement(auction.getPriceDecrement());
        dto.setMinimumPrice(auction.getMinimumPrice());
        dto.setCurrentBidderId(auction.getCurrentBidderId());
        dto.setImageUrls(auction.getImageUrls());
        dto.setStatus(auction.getStatus().name());
        return dto;
    }
}