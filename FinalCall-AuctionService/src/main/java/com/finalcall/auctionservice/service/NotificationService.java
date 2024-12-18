package com.finalcall.auctionservice.service;

import com.finalcall.auctionservice.dto.NotificationDTO;
import com.finalcall.auctionservice.entity.Notification;
import com.finalcall.auctionservice.repository.NotificationRepository;
import com.finalcall.auctionservice.websocket.ConsolidatedWebSocketHandler;
import com.finalcall.auctionservice.event.AuctionEvents.NotificationEvent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;


@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private ConsolidatedWebSocketHandler webSocketHandler;

    /**
     * Handle notification events: persist and broadcast.
     */
    @EventListener
    public void handleNotificationEvent(NotificationEvent event) {
        NotificationDTO notificationDTO = event.getNotificationDTO();
        Notification notification = new Notification(
                event.getUserId(),
                notificationDTO.getMessage(),
                notificationDTO.getType(),
                notificationDTO.getLink()
        );
        Notification saved = notificationRepository.save(notification);

        notificationDTO.setId(saved.getId());
        notificationDTO.setTimestamp(saved.getTimestamp());

        // Broadcast notification to user's notification channel
        webSocketHandler.sendNotificationToUser(event.getUserId(), notificationDTO);
    }
}
