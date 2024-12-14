package com.finalcall.auctionservice.service;

import com.finalcall.auctionservice.client.CatalogueServiceClient;
import com.finalcall.auctionservice.client.AuthenticationServiceClient;
import com.finalcall.auctionservice.dto.*;
import com.finalcall.auctionservice.entity.*;
import com.finalcall.auctionservice.event.AuctionUpdatedEvent;
import com.finalcall.auctionservice.exception.AuctionNotActiveException;
import com.finalcall.auctionservice.exception.AuctionNotFoundException;
import com.finalcall.auctionservice.exception.InvalidBidException;
import com.finalcall.auctionservice.repository.AuctionRepository;
import com.finalcall.auctionservice.repository.BidRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AuctionService {
    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private BidRepository bidRepository;

    @Autowired
    private CatalogueServiceClient catalogueServiceClient;

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
     * Places a bid on an auction.
     *
     * @param auctionId  The ID of the auction.
     * @param bidRequest The bid details.
     * @return BidResponse indicating the result.
     */
    @Transactional
    public BidResponse placeBid(Long auctionId, BidRequest bidRequest) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new AuctionNotFoundException("Auction not found with ID: " + auctionId));

        // Check if auction is active
        if (auction.getStatus() != AuctionStatus.ACTIVE) {
            throw new AuctionNotActiveException("This auction is no longer active");
        }

        // Check if auction has ended
        if (LocalDateTime.now().isAfter(auction.getAuctionEndTime())) {
            auction.setStatus(AuctionStatus.ENDED);
            auctionRepository.save(auction);
            throw new AuctionNotActiveException("This auction has ended");
        }

        if (auction.getAuctionType() == AuctionType.FORWARD) {
            return handleForwardAuctionBid(auction, bidRequest);
        } else if (auction.getAuctionType() == AuctionType.DUTCH) {
            return handleDutchAuctionBid(auction, bidRequest);
        }

        throw new UnsupportedOperationException("Unsupported auction type");
    }

    /**
     * Handles bid placement for forward auctions.
     */
    private BidResponse handleForwardAuctionBid(Auction auction, BidRequest bidRequest) {
        // Validate bid amount
        if (bidRequest.getBidAmount() <= auction.getCurrentBidPrice()) {
            throw new InvalidBidException("Bid must be higher than current bid of $" + 
                auction.getCurrentBidPrice());
        }

        // Update auction
        auction.setCurrentBidPrice(bidRequest.getBidAmount());
        auction.setCurrentBidderId(bidRequest.getBidderId());
        
        // Save bid
        Bid bid = new Bid(bidRequest.getBidAmount(), auction.getId(), bidRequest.getBidderId());
        bidRepository.save(bid);
        
        // Save auction
        auctionRepository.save(auction);

        // Publish event
        eventPublisher.publishEvent(new AuctionUpdatedEvent(this, auction));

        return new BidResponse("Bid placed successfully", auction.getCurrentBidPrice());
    }

    /**
     * Handles bid placement for Dutch auctions.
     */
    private BidResponse handleDutchAuctionBid(Auction auction, BidRequest bidRequest) {
        // For Dutch auctions, the bid must equal the current price
        if (bidRequest.getBidAmount().compareTo(auction.getCurrentBidPrice()) != 0) {
            throw new InvalidBidException("For Dutch auctions, bid must equal current price of $" + 
                auction.getCurrentBidPrice());
        }

        // End the auction immediately as Dutch auctions end on first valid bid
        auction.setStatus(AuctionStatus.ENDED);
        auction.setCurrentBidderId(bidRequest.getBidderId());
        
        // Save bid
        Bid bid = new Bid(bidRequest.getBidAmount(), auction.getId(), bidRequest.getBidderId());
        bidRepository.save(bid);
        
        // Save auction
        auctionRepository.save(auction);

        // Publish event
        eventPublisher.publishEvent(new AuctionUpdatedEvent(this, auction));

        return new BidResponse("Dutch auction won", auction.getCurrentBidPrice());
    }

    /**
     * Scheduled task to decrease Dutch auction prices.
     */
    @Scheduled(fixedRate = 60000) // Every minute
    @Transactional
    public void decreaseDutchAuctionPrices() {
        List<Auction> activeDutchAuctions = auctionRepository
            .findByAuctionTypeAndStatus(AuctionType.DUTCH, AuctionStatus.ACTIVE);

        for (Auction auction : activeDutchAuctions) {
            double newPrice = auction.getCurrentBidPrice() - auction.getPriceDecrement();
            
            if (newPrice <= auction.getMinimumPrice()) {
                // End auction if price would go below minimum
                auction.setCurrentBidPrice(auction.getMinimumPrice());
                auction.setStatus(AuctionStatus.ENDED);
            } else {
                auction.setCurrentBidPrice(newPrice);
            }
            
            auctionRepository.save(auction);
            eventPublisher.publishEvent(new AuctionUpdatedEvent(this, auction));
        }
    }

    /**
     * Maps Auction entity to AuctionDTO.
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

    /**
     * Finds an auction by its associated item ID.
     */
    public Optional<Auction> findByItemId(Long itemId) {
        return auctionRepository.findByItemId(itemId);
    }

    /**
     * Retrieves all bids for a specific auction.
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
     * Retrieves bids for a specific user.
     */
    public List<BidDTO> getUserBids(Long userId) {
        List<Bid> userBids = bidRepository.findAllByBidderId(userId);

        return userBids.stream()
            .map(bid -> {
                Auction auction = auctionRepository.findById(bid.getAuctionId())
                    .orElseThrow(() -> new AuctionNotFoundException("Auction not found"));
                
                BidDTO bidDTO = new BidDTO();
                bidDTO.setId(bid.getId());
                bidDTO.setAmount(bid.getAmount());
                bidDTO.setBidderId(bid.getBidderId());
                bidDTO.setTimestamp(bid.getTimestamp());
                
                // Map auction details
                AuctionDTO auctionDTO = mapToDTO(auction);
                
                try {
                    // Fetch item details from the Catalogue Service
                    ItemDTO itemDTO = catalogueServiceClient.getItemById(auction.getItemId());
                    auctionDTO.setItem(itemDTO);
                } catch (Exception e) {
                    // Handle potential errors in fetching item details
                    ItemDTO itemDTO = new ItemDTO();
                    itemDTO.setId(auction.getItemId());
                    itemDTO.setName("Unknown Item");
                    auctionDTO.setItem(itemDTO);
                }
                
                bidDTO.setAuction(auctionDTO);

                return bidDTO;
            })
            .collect(Collectors.toList());
    }
}