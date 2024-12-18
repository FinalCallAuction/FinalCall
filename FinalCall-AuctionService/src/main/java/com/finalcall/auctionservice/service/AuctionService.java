package com.finalcall.auctionservice.service;

import com.finalcall.auctionservice.dto.*;
import com.finalcall.auctionservice.entity.*;
import com.finalcall.auctionservice.exception.AuctionNotActiveException;
import com.finalcall.auctionservice.exception.AuctionNotFoundException;
import com.finalcall.auctionservice.exception.InvalidBidException;
import com.finalcall.auctionservice.exception.InvalidDecrementException;
import com.finalcall.auctionservice.repository.AuctionRepository;
import com.finalcall.auctionservice.repository.BidRepository;
import com.finalcall.auctionservice.event.AuctionEvents.AuctionUpdatedEvent;
import com.finalcall.auctionservice.event.AuctionEvents.NotificationEvent;

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
    private ItemService itemService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private WebSocketBroadcastService webSocketBroadcastService;

    @Transactional
    public Auction createAuction(AuctionDTO auctionDTO) {
        // Verify item exists first
        ItemDTO item = itemService.getItemById(auctionDTO.getItemId());
        if (item == null) {
            throw new IllegalArgumentException("Item not found");
        }

        Auction auction = new Auction();
        auction.setItemId(auctionDTO.getItemId());
        auction.setAuctionType(auctionDTO.getAuctionType());
        auction.setStartingBidPrice(auctionDTO.getStartingBidPrice());
        auction.setCurrentBidPrice(auctionDTO.getStartingBidPrice());
        auction.setAuctionEndTime(auctionDTO.getAuctionEndTime());
        auction.setSellerId(auctionDTO.getSellerId());
        auction.setStartTime(LocalDateTime.now());
        auction.setStatus(AuctionStatus.ACTIVE);

        if (auctionDTO.getAuctionType() == AuctionType.DUTCH) {
            auction.setMinimumPrice(auctionDTO.getMinimumPrice());
        }

        Auction saved = auctionRepository.save(auction);

        // Prepare the response DTO
        AuctionDTO responseDTO = mapToDTO(saved);
        responseDTO.setItem(item);

        // Get seller information
        UserDTO seller = itemService.getUserById(saved.getSellerId());
        responseDTO.setSellerName(seller.getUsername());

        // Broadcast the new auction
        webSocketBroadcastService.broadcastNewAuction(responseDTO);

        return saved;
    }

    @Transactional
    public BidResponse placeBid(Long auctionId, BidRequest bidRequest) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new AuctionNotFoundException("Auction not found with ID: " + auctionId));

        // Check if auction is active
        if (auction.getStatus() != AuctionStatus.ACTIVE) {
            throw new AuctionNotActiveException("This auction is no longer active");
        }

        // Check if auction has ended
        if (auction.getAuctionEndTime() != null && LocalDateTime.now().isAfter(auction.getAuctionEndTime())) {
            auction.setStatus(AuctionStatus.ENDED);
            auctionRepository.save(auction);
            throw new AuctionNotActiveException("This auction has ended");
        }

        BidResponse response;
        if (auction.getAuctionType() == AuctionType.FORWARD) {
            response = handleForwardAuctionBid(auction, bidRequest);
        } else if (auction.getAuctionType() == AuctionType.DUTCH) {
            response = handleDutchAuctionBid(auction, bidRequest);
        } else {
            throw new UnsupportedOperationException("Unsupported auction type");
        }

        // After successful bid placement, broadcast the update
        AuctionDTO updatedAuction = mapToDTO(auction);
        try {
            ItemDTO itemDTO = itemService.getItemById(auction.getItemId());
            updatedAuction.setItem(itemDTO);
        } catch (Exception e) {
            // If item fetch fails, proceed with what we have
        }

        List<BidDTO> biddingHistory = getBidsForAuction(auctionId);
        webSocketBroadcastService.broadcastAuctionUpdate(auctionId, updatedAuction, biddingHistory);

        return response;
    }

    private BidResponse handleForwardAuctionBid(Auction auction, BidRequest bidRequest) {
        if (bidRequest.getBidAmount() <= auction.getCurrentBidPrice()) {
            throw new InvalidBidException("Bid must be higher than current bid of $" + 
                    auction.getCurrentBidPrice());
        }

        ItemDTO item = itemService.getItemById(auction.getItemId());
        UserDTO bidder = itemService.getUserById(bidRequest.getBidderId());

        BidResponse bidResponse = new BidResponse("Bid placed successfully", bidRequest.getBidAmount());

        // Notify previous highest bidder if exists
        if (auction.getCurrentBidderId() != null && !auction.getCurrentBidderId().equals(bidRequest.getBidderId())) {
            NotificationDTO outbidNotification = new NotificationDTO(
                String.format("You have been outbid on %s. Current bid is $%.2f", item.getName(), bidRequest.getBidAmount()),
                "OUTBID"
            );
            outbidNotification.setLink("/my-bids");
            eventPublisher.publishEvent(new NotificationEvent(this, auction.getCurrentBidderId(), outbidNotification));
            bidResponse.addNotification(outbidNotification);
        }

        // Notify seller of new bid
        NotificationDTO newBidNotification = new NotificationDTO(
            String.format("New bid of $%.2f placed on your item %s", bidRequest.getBidAmount(), item.getName()),
            "NEW_BID"
        );
        newBidNotification.setLink("/items");
        eventPublisher.publishEvent(new NotificationEvent(this, auction.getSellerId(), newBidNotification));
        bidResponse.addNotification(newBidNotification);

        // Update auction and save bid
        auction.setCurrentBidPrice(bidRequest.getBidAmount());
        auction.setCurrentBidderId(bidRequest.getBidderId());
        Bid bid = new Bid(bidRequest.getBidAmount(), auction.getId(), bidRequest.getBidderId());
        bidRepository.save(bid);
        auctionRepository.save(auction);

        return bidResponse;
    }

    private BidResponse handleDutchAuctionBid(Auction auction, BidRequest bidRequest) {
        if (!bidRequest.getBidAmount().equals(auction.getCurrentBidPrice())) {
            throw new InvalidBidException("For Dutch auctions, bid must equal current price of $" +
                    auction.getCurrentBidPrice());
        }

        ItemDTO item = itemService.getItemById(auction.getItemId());
        UserDTO bidder = itemService.getUserById(bidRequest.getBidderId());

        BidResponse bidResponse = new BidResponse("Dutch auction won", bidRequest.getBidAmount());

        // Notify seller of sale
        NotificationDTO sellerNotification = new NotificationDTO(
            String.format("Item %s sold at $%.2f", item.getName(), bidRequest.getBidAmount()),
            "DUTCH_AUCTION_SOLD"
        );
        sellerNotification.setLink("/profile");
        eventPublisher.publishEvent(new NotificationEvent(this, auction.getSellerId(), sellerNotification));
        bidResponse.addNotification(sellerNotification);

        // Save final bid and end auction
        Bid bid = new Bid(bidRequest.getBidAmount(), auction.getId(), bidRequest.getBidderId());
        bid.setTimestamp(LocalDateTime.now());
        bidRepository.save(bid);

        auction.setStatus(AuctionStatus.ENDED);
        auction.setCurrentBidderId(bidRequest.getBidderId());
        auctionRepository.save(auction);

        return bidResponse;
    }

    @Transactional
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
        dto.setImageUrls(new ArrayList<>(auction.getImageUrls())); 
        dto.setStatus(auction.getStatus().name());
        return dto;
    }

    public Optional<Auction> findByItemId(Long itemId) {
        return auctionRepository.findByItemId(itemId);
    }

    public List<BidDTO> getBidsForAuction(Long auctionId) {
        Optional<Auction> auctionOpt = auctionRepository.findById(auctionId);
        if (!auctionOpt.isPresent()) {
            return Collections.emptyList();
        }

        Auction auction = auctionOpt.get();
        List<Bid> bids = bidRepository.findByAuctionIdOrderByTimestampDesc(auctionId);

        if (bids.isEmpty()) {
            return Collections.emptyList();
        }

        List<BidDTO> bidDTOs = new ArrayList<>();
        Double runningAmount = auction.getStartingBidPrice();

        for (int i = bids.size() - 1; i >= 0; i--) {
            Bid bid = bids.get(i);
            UserDTO user = itemService.getUserById(bid.getBidderId());

            BidDTO bidDTO = new BidDTO(
                bid.getId(),
                bid.getAmount(),
                bid.getBidderId(),
                user != null ? user.getUsername() : "Unknown",
                bid.getTimestamp()
            );

            if (auction.getAuctionType() == AuctionType.DUTCH && bid.getBidderId().equals(auction.getSellerId())) {
                bidDTO.setType("PRICE_CHANGE");
                bidDTO.setPreviousAmount(runningAmount);
            } else {
                bidDTO.setType("BID");
            }

            runningAmount = bid.getAmount();
            bidDTOs.add(bidDTO);
        }

        Collections.reverse(bidDTOs);
        return bidDTOs;
    }

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

                AuctionDTO auctionDTO = mapToDTO(auction);
                try {
                    ItemDTO itemDTO = itemService.getItemById(auction.getItemId());
                    auctionDTO.setItem(itemDTO);
                } catch (Exception e) {
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

    @Transactional
    public BidResponse manualDecrement(Long auctionId, Long userId, Double decrementAmount) {
        Auction auction = auctionRepository.findById(auctionId)
            .orElseThrow(() -> new AuctionNotFoundException("Auction not found"));

        if (auction.getAuctionType() != AuctionType.DUTCH || auction.getStatus() != AuctionStatus.ACTIVE) {
            throw new InvalidDecrementException("Not an active Dutch auction");
        }

        if (!auction.getSellerId().equals(userId)) {
            throw new InvalidDecrementException("Only the seller can decrease the price");
        }

        if (decrementAmount <= 0) {
            throw new InvalidDecrementException("Decrement amount must be positive");
        }

        double oldPrice = auction.getCurrentBidPrice();
        double newPrice = oldPrice - decrementAmount;

        if (newPrice < auction.getMinimumPrice()) {
            throw new InvalidDecrementException(
                String.format("Cannot decrease below minimum price of $%.2f", auction.getMinimumPrice())
            );
        }

        // Store price change as a "bid"
        Bid priceChange = new Bid(newPrice, auction.getId(), userId);
        priceChange.setTimestamp(LocalDateTime.now());
        bidRepository.save(priceChange);

        // Update auction price
        auction.setCurrentBidPrice(newPrice);
        auctionRepository.save(auction);

        // Publish update event
        eventPublisher.publishEvent(new AuctionUpdatedEvent(this, auction));

        // Notify bidders about the price decrement
        ItemDTO item = itemService.getItemById(auction.getItemId());
        List<Bid> bids = bidRepository.findByAuctionIdOrderByTimestampDesc(auctionId);
        Set<Long> bidderIds = bids.stream()
            .map(Bid::getBidderId)
            .filter(id -> !id.equals(userId))
            .collect(Collectors.toSet());

        NotificationDTO priceDecrementNotification = new NotificationDTO(
            String.format("The price for auction '%s' has been decreased by $%.2f to $%.2f.",
                item.getName(), decrementAmount, newPrice),
            "PRICE_DECREMENT"
        );
        priceDecrementNotification.setLink("/items/" + auction.getItemId());

        bidderIds.forEach(bidderId ->
            eventPublisher.publishEvent(new NotificationEvent(this, bidderId, priceDecrementNotification))
        );

        BidResponse bidResponse = new BidResponse("Price decreased successfully", newPrice);
        bidResponse.addNotification(priceDecrementNotification);

        return bidResponse;
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void checkAuctionEndings() {
        LocalDateTime now = LocalDateTime.now();
        List<Auction> endingAuctions = auctionRepository.findByStatusAndAuctionEndTimeLessThan(
            AuctionStatus.ACTIVE, now
        );

        for (Auction auction : endingAuctions) {
            auction.setStatus(AuctionStatus.ENDED);
            auctionRepository.save(auction);

            ItemDTO item = itemService.getItemById(auction.getItemId());

            BidResponse auctionEndResponse = new BidResponse("Auction ended", auction.getCurrentBidPrice());

            // Winner notification
            if (auction.getCurrentBidderId() != null) {
                NotificationDTO winnerNotification = new NotificationDTO(
                    String.format("Congratulations! You won the auction for %s", item.getName()),
                    "AUCTION_WON"
                );
                winnerNotification.setLink("/my-bids");
                eventPublisher.publishEvent(new NotificationEvent(this, auction.getCurrentBidderId(), winnerNotification));
                auctionEndResponse.addNotification(winnerNotification);
            }

            // Notify others that auction ended
            List<Bid> bids = bidRepository.findByAuctionIdOrderByTimestampDesc(auction.getId());
            Set<Long> loserIds = bids.stream()
                .map(Bid::getBidderId)
                .filter(id -> !id.equals(auction.getCurrentBidderId()))
                .collect(Collectors.toSet());

            NotificationDTO loserNotification = new NotificationDTO(
                String.format("The auction for %s has ended.", item.getName()),
                "AUCTION_ENDED"
            );
            loserNotification.setLink("/my-bids");

            loserIds.forEach(loserId ->
                eventPublisher.publishEvent(new NotificationEvent(this, loserId, loserNotification))
            );

            auctionEndResponse.addNotification(loserNotification);
        }
    }

    public Optional<Auction> findByAuctionId(Long auctionId) {
        return auctionRepository.findById(auctionId);
    }
}