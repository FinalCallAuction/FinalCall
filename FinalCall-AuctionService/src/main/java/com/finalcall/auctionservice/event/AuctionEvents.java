package com.finalcall.auctionservice.event;

import com.finalcall.auctionservice.dto.NotificationDTO;
import com.finalcall.auctionservice.entity.Auction;
import org.springframework.context.ApplicationEvent;
import org.springframework.web.socket.WebSocketSession;

public class AuctionEvents {
    public static class AuctionUpdatedEvent extends ApplicationEvent {
        private final Auction auction;

        public AuctionUpdatedEvent(Object source, Auction auction) {
            super(source);
            this.auction = auction;
        }

        public Auction getAuction() {
            return auction;
        }
    }

    public static class NotificationEvent extends ApplicationEvent {
        private final Long userId;
        private final NotificationDTO notificationDTO;

        public NotificationEvent(Object source, Long userId, NotificationDTO notificationDTO) {
            super(source);
            this.userId = userId;
            this.notificationDTO = notificationDTO;
        }

        public Long getUserId() {
            return userId;
        }

        public NotificationDTO getNotificationDTO() {
            return notificationDTO;
        }
    }

    public static class WebSocketEvent extends ApplicationEvent {
        private final String type;
        private final Object payload;
        private final WebSocketSession session;
        private final Long auctionId;

        public WebSocketEvent(Object source, String type, Object payload, WebSocketSession session, Long auctionId) {
            super(source);
            this.type = type;
            this.payload = payload;
            this.session = session;
            this.auctionId = auctionId;
        }

        public String getType() { return type; }
        public Object getPayload() { return payload; }
        public WebSocketSession getSession() { return session; }
        public Long getAuctionId() { return auctionId; }
    }
}