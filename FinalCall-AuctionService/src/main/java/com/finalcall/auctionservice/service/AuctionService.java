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
     */
    @Transactional
    public Auction createAuction(AuctionDTO auctionDTO) {
        Auction auction = new Auction();
        auction.setItemId(auctionDTO.getItemId());
        auction.setAuctionType(auctionDTO.getAuctionType());
        auction.setStartingBidPrice(auctionDTO.getStartingBidPrice());

        if (auctionDTO.getAuctionType() == AuctionType.FORWARD) {
            auction.setCurrentBidPrice(auctionDTO.getStartingBidPrice());
        } else if (auctionDTO.getAuctionType() == AuctionType.DUTCH) {
            auction.setCurrentBidPrice(auctionDTO.getStartingBidPrice());
            auction.setPriceDecrement(auctionDTO.getPriceDecrement());
            auction.setMinimumPrice(auctionDTO.getMinimumPrice());
        }

        auction.setAuctionEndTime(auctionDTO.getAuctionEndTime());
        auction.setSellerId(auctionDTO.getSellerId());
        auction.setStartTime(auctionDTO.getStartTime());
        auction.setStatus(AuctionStatus.ACTIVE);

        Auction savedAuction = auctionRepository.save(auction);
        eventPublisher.publishEvent(new AuctionUpdatedEvent(this, savedAuction));
        return savedAuction;
    }

    /**
     * Places a bid on an auction.
     */
    @Transactional
    public BidResponse placeBid(Long auctionId, BidRequest bidRequest) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new AuctionNotFoundException("Auction not found with ID: " + auctionId));

        // Validate auction status
        if (auction.getStatus() != AuctionStatus.ACTIVE) {
            throw new AuctionNotActiveException("This auction is no longer active");
        }

        // Check if auction has ended
        if (LocalDateTime.now().isAfter(auction.getAuctionEndTime())) {
            auction.setStatus(AuctionStatus.ENDED);
            auctionRepository.save(auction);
            eventPublisher.publishEvent(new AuctionUpdatedEvent(this, auction));
            throw new AuctionNotActiveException("This auction has ended");
        }

        // Prevent seller from bidding on their own auction
        if (auction.getSellerId().equals(bidRequest.getBidderId())) {
            throw new InvalidBidException("Sellers cannot bid on their own auctions");
        }

        // Process bid based on auction type
        BidResponse response;
        if (auction.getAuctionType() == AuctionType.FORWARD) {
            response = handleForwardAuctionBid(auction, bidRequest);
        } else if (auction.getAuctionType() == AuctionType.DUTCH) {
            response = handleDutchAuctionBid(auction, bidRequest);
        } else {
            throw new UnsupportedOperationException("Unsupported auction type");
        }

        // Enhance response with additional details
        try {
            UserDTO bidder = authenticationServiceClient.getUserById(bidRequest.getBidderId());
            response.setBidderName(bidder.getUsername());
            response.setBidTimestamp(LocalDateTime.now());
            response.setAuctionStatus(auction.getStatus().name());
            response.setIsWinningBid(auction.getCurrentBidderId().equals(bidRequest.getBidderId()));
        } catch (Exception e) {
            // Log error but continue with basic response
        }

        return response;
    }
    /**
     * Handles bid placement for forward auctions.
     */
    private BidResponse handleForwardAuctionBid(Auction auction, BidRequest bidRequest) {
        if (bidRequest.getBidAmount() <= auction.getCurrentBidPrice()) {
            throw new InvalidBidException("Bid must be higher than current bid of $" + 
                auction.getCurrentBidPrice());
        }

        auction.setCurrentBidPrice(bidRequest.getBidAmount());
        auction.setCurrentBidderId(bidRequest.getBidderId());
        
        Bid bid = new Bid(bidRequest.getBidAmount(), auction.getId(), bidRequest.getBidderId());
        bidRepository.save(bid);
        
        auctionRepository.save(auction);
        eventPublisher.publishEvent(new AuctionUpdatedEvent(this, auction));

        return new BidResponse("Bid placed successfully", auction.getCurrentBidPrice());
    }

    /**
     * Handles bid placement for Dutch auctions.
     */
    private BidResponse handleDutchAuctionBid(Auction auction, BidRequest bidRequest) {
        if (bidRequest.getBidAmount().compareTo(auction.getCurrentBidPrice()) != 0) {
            throw new InvalidBidException("For Dutch auctions, bid must equal current price of $" + 
                auction.getCurrentBidPrice());
        }

        auction.setStatus(AuctionStatus.ENDED);
        auction.setCurrentBidderId(bidRequest.getBidderId());
        
        Bid bid = new Bid(bidRequest.getBidAmount(), auction.getId(), bidRequest.getBidderId());
        bidRepository.save(bid);
        
        auctionRepository.save(auction);
        eventPublisher.publishEvent(new AuctionUpdatedEvent(this, auction));

        return new BidResponse("Dutch auction won", auction.getCurrentBidPrice());
    }

    /**
     * Scheduled task to check and update auction statuses.
     */
    @Scheduled(fixedRate = 60000) // Every minute
    @Transactional
    public void updateAuctionStatuses() {
        List<Auction> activeAuctions = auctionRepository
            .findByStatus(AuctionStatus.ACTIVE);

        LocalDateTime now = LocalDateTime.now();
        for (Auction auction : activeAuctions) {
            if (now.isAfter(auction.getAuctionEndTime())) {
                auction.setStatus(AuctionStatus.ENDED);
                auctionRepository.save(auction);
                eventPublisher.publishEvent(new AuctionUpdatedEvent(this, auction));
            }
        }
    }

    /**
     * Scheduled task to decrease Dutch auction prices.
     */
    @Scheduled(fixedRate = 60000) // Every minute
    @Transactional
    public void decreaseDutchAuctionPrices() {
        List<Auction> activeDutchAuctions = auctionRepository
            .findByAuctionTypeAndStatus(AuctionType.DUTCH, AuctionStatus.ACTIVE);

        LocalDateTime now = LocalDateTime.now();
        for (Auction auction : activeDutchAuctions) {
            // First check if auction should end due to time
            if (now.isAfter(auction.getAuctionEndTime())) {
                auction.setStatus(AuctionStatus.ENDED);
                auctionRepository.save(auction);
                eventPublisher.publishEvent(new AuctionUpdatedEvent(this, auction));
                continue;
            }

            double newPrice = auction.getCurrentBidPrice() - auction.getPriceDecrement();
            
            if (newPrice <= auction.getMinimumPrice()) {
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

    /**
     * Maps Auction entity to AuctionDTO.
     */
//    public AuctionDTO mapToDTO(Auction auction) {
//        AuctionDTO dto = new AuctionDTO();
//        dto.setId(auction.getId());
//        dto.setItemId(auction.getItemId());
//        dto.setAuctionType(auction.getAuctionType());
//        dto.setStartingBidPrice(auction.getStartingBidPrice());
//        dto.setCurrentBidPrice(auction.getCurrentBidPrice());
//        dto.setAuctionEndTime(auction.getAuctionEndTime());
//        dto.setSellerId(auction.getSellerId());
//        dto.setStartTime(auction.getStartTime());
//        dto.setPriceDecrement(auction.getPriceDecrement());
//        dto.setMinimumPrice(auction.getMinimumPrice());
//        dto.setCurrentBidderId(auction.getCurrentBidderId());
//        dto.setImageUrls(auction.getImageUrls());
//        dto.setStatus(auction.getStatus().name());
//        return dto;
//    }
    
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

        // Fetch and set current bidder details if available
        if (auction.getCurrentBidderId() != null) {
            try {
                UserDTO currentBidder = authenticationServiceClient.getUserById(auction.getCurrentBidderId());
                dto.setCurrentBidderName(currentBidder.getUsername());
                dto.setCurrentBidderDetails(currentBidder);
            } catch (Exception e) {
                // Log error but continue with available data
                dto.setCurrentBidderName("Unknown Bidder");
            }
        }

        // Fetch and set seller details
        try {
            UserDTO seller = authenticationServiceClient.getUserById(auction.getSellerId());
            dto.setSellerName(seller.getUsername());
            dto.setSellerDetails(seller);
        } catch (Exception e) {
            // Log error but continue with available data
            dto.setSellerName("Unknown Seller");
        }

        // Include latest bid information
        List<Bid> bids = bidRepository.findByAuctionIdOrderByTimestampDesc(auction.getId());
        if (!bids.isEmpty()) {
            Bid latestBid = bids.get(0);
            dto.setLatestBidTimestamp(latestBid.getTimestamp());
            dto.setTotalBids((long) bids.size());
        }

        // Add auction status details
        dto.setIsEnded(auction.getStatus() == AuctionStatus.ENDED);
        dto.setTimeRemaining(auction.getAuctionEndTime().isAfter(LocalDateTime.now()) ? 
            auction.getAuctionEndTime().toString() : "Ended");

        return dto;
    }

    /**
     * Finds an auction by its associated item ID.
     */
    public Optional<Auction> findByItemId(Long itemId) {
        return auctionRepository.findByItemId(itemId);
    }

    /**
     * Checks if an auction is ended and if the specified user is the winner.
     */
    public boolean isAuctionEndedAndUserIsWinner(Long auctionId, Long userId) {
        Optional<Auction> auctionOpt = auctionRepository.findById(auctionId);
        if (auctionOpt.isEmpty()) {
            return false;
        }
        
        Auction auction = auctionOpt.get();
        return auction.getStatus() == AuctionStatus.ENDED &&
               auction.getCurrentBidderId() != null &&
               auction.getCurrentBidderId().equals(userId);
    }

    /**
     * Get auction by ID.
     */
    public Optional<Auction> getAuctionById(Long auctionId) {
        return auctionRepository.findById(auctionId);
    }
}