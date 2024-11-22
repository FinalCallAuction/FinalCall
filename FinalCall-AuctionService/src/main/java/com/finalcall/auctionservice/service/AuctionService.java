// src/main/java/com/finalcall/auctionservice/service/AuctionService.java

package com.finalcall.auctionservice.service;

import com.finalcall.auctionservice.dto.AuctionDTO;
import com.finalcall.auctionservice.dto.BidRequest;
import com.finalcall.auctionservice.dto.BidResponse;
import com.finalcall.auctionservice.entity.Auction;
import com.finalcall.auctionservice.entity.AuctionStatus;
import com.finalcall.auctionservice.entity.AuctionType;
import com.finalcall.auctionservice.entity.Bid;
import com.finalcall.auctionservice.exception.AuctionNotActiveException;
import com.finalcall.auctionservice.exception.AuctionNotFoundException;
import com.finalcall.auctionservice.exception.InvalidBidException;
import com.finalcall.auctionservice.repository.AuctionRepository;
import com.finalcall.auctionservice.repository.BidRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AuctionService {

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private BidRepository bidRepository;

    /**
     * Create a new auction.
     *
     * @param auctionDTO Details of the auction.
     * @return The created Auction entity.
     */
    public Auction createAuction(AuctionDTO auctionDTO) {
        Auction auction = new Auction();
        auction.setItemId(auctionDTO.getItemId());
        auction.setAuctionType(auctionDTO.getAuctionType());
        auction.setStartingBidPrice(auctionDTO.getStartingBidPrice());
        auction.setCurrentBidPrice(auctionDTO.getCurrentBidPrice()); // Set currentBidPrice
        auction.setAuctionEndTime(auctionDTO.getAuctionEndTime());
        auction.setSellerId(auctionDTO.getSellerId());
        auction.setStartTime(auctionDTO.getStartTime());

        return auctionRepository.save(auction);
    }

    /**
     * Find an auction by item ID.
     *
     * @param itemId The ID of the item.
     * @return Optional containing the Auction if found.
     */
    public Optional<Auction> findByItemId(Long itemId) {
        return auctionRepository.findByItemId(itemId);
    }

    /**
     * Update the current bid price of an auction.
     *
     * @param auctionId       The ID of the auction.
     * @param newBidPrice     The new bid price.
     * @return The updated Auction entity.
     */
    public Optional<Auction> updateCurrentBidPrice(Long auctionId, Double newBidPrice) {
        Optional<Auction> auctionOpt = auctionRepository.findById(auctionId);
        if (auctionOpt.isPresent()) {
            Auction auction = auctionOpt.get();
            if (newBidPrice > auction.getCurrentBidPrice()) {
                auction.setCurrentBidPrice(newBidPrice);
                auctionRepository.save(auction);
                return Optional.of(auction);
            }
        }
        return Optional.empty();
    }

    /**
     * Place a bid on an auction.
     *
     * @param auctionId  The ID of the auction.
     * @param bidRequest The bid details.
     * @return BidResponse indicating the result.
     */
    public BidResponse placeBid(Long auctionId, BidRequest bidRequest) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new AuctionNotFoundException("Auction not found with ID: " + auctionId));

        if (auction.getStatus() != AuctionStatus.ACTIVE) {
            throw new AuctionNotActiveException("Auction is not active");
        }

        if (auction.getAuctionType() == AuctionType.FORWARD) {
            return handleForwardAuctionBid(auction, bidRequest);
        } else if (auction.getAuctionType() == AuctionType.DUTCH) {
            return handleDutchAuctionBid(auction, bidRequest);
        } else {
            throw new UnsupportedOperationException("Unsupported auction type");
        }
    }

    /**
     * Handle bid placement for forward auctions.
     *
     * @param auction    The auction entity.
     * @param bidRequest The bid details.
     * @return BidResponse indicating success.
     */
    private BidResponse handleForwardAuctionBid(Auction auction, BidRequest bidRequest) {
        if (bidRequest.getBidAmount() == null || bidRequest.getBidAmount() <= auction.getCurrentBidPrice()) {
            throw new InvalidBidException("Bid must be higher than current bid");
        }

        auction.setCurrentBidPrice(bidRequest.getBidAmount());
        auction.setCurrentBidderId(bidRequest.getBidderId());
        auctionRepository.save(auction);

        // Save the bid
        Bid bid = new Bid(bidRequest.getBidAmount(), auction.getId(), bidRequest.getBidderId());
        bidRepository.save(bid);

        return new BidResponse("Bid placed successfully", auction.getCurrentBidPrice());
    }

    /**
     * Handle bid placement for Dutch auctions.
     *
     * @param auction    The auction entity.
     * @param bidRequest The bid details.
     * @return BidResponse indicating success.
     */
    private BidResponse handleDutchAuctionBid(Auction auction, BidRequest bidRequest) {
        // In Dutch auctions, the first bidder wins at the current price
        auction.setStatus(AuctionStatus.ENDED);
        auction.setCurrentBidderId(bidRequest.getBidderId());
        auctionRepository.save(auction);

        // Save the bid
        Bid bid = new Bid(auction.getCurrentBidPrice(), auction.getId(), bidRequest.getBidderId());
        bidRepository.save(bid);

        return new BidResponse("Auction won at price: " + auction.getCurrentBidPrice(), auction.getCurrentBidPrice());
    }

    /**
     * Scheduled task to decrease prices in active Dutch auctions.
     * Runs every minute.
     */
    @Scheduled(fixedRate = 60000) // Every minute
    public void decreaseDutchAuctionPrices() {
        List<Auction> activeDutchAuctions = auctionRepository.findByAuctionTypeAndStatus(
                AuctionType.DUTCH, AuctionStatus.ACTIVE);

        for (Auction auction : activeDutchAuctions) {
            Double newPrice = auction.getCurrentBidPrice() - auction.getPriceDecrement();
            if (newPrice <= auction.getMinimumPrice()) {
                auction.setCurrentBidPrice(auction.getMinimumPrice());
                auction.setStatus(AuctionStatus.ENDED);
            } else {
                auction.setCurrentBidPrice(newPrice);
            }
            auctionRepository.save(auction);
        }
    }

    /**
     * Retrieve all bids for a specific auction.
     *
     * @param auctionId The ID of the auction.
     * @return List of bids.
     */
    public List<Bid> getBidsForAuction(Long auctionId) {
        return bidRepository.findByAuctionIdOrderByTimestampDesc(auctionId);
    }

    /**
     * Map Auction entity to AuctionDTO.
     *
     * @param auction The Auction entity.
     * @return AuctionDTO object.
     */
    private AuctionDTO mapToDTO(Auction auction) {
        AuctionDTO dto = new AuctionDTO();
        dto.setItemId(auction.getItemId());
        dto.setAuctionType(auction.getAuctionType());
        dto.setStartingBidPrice(auction.getStartingBidPrice());
        dto.setAuctionEndTime(auction.getAuctionEndTime());
        dto.setSellerId(auction.getSellerId());
        dto.setStartTime(auction.getStartTime());
        return dto;
    }
}
