package com.finalcall.auctionservice.controller;

import com.finalcall.auctionservice.dto.NotificationDTO;
import com.finalcall.auctionservice.entity.Notification;
import com.finalcall.auctionservice.repository.NotificationRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "http://localhost:3000")
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getAllNotifications(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        List<Notification> notifications = notificationRepository.findByUserIdOrderByTimestampDesc(userId);
        return ResponseEntity.ok(notifications.stream().map(this::convertToDTO).collect(Collectors.toList()));
    }

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDTO>> getUnreadNotifications(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        List<Notification> notifications = notificationRepository.findByUserIdAndReadFalseOrderByTimestampDesc(userId);
        return ResponseEntity.ok(notifications.stream().map(this::convertToDTO).collect(Collectors.toList()));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id, Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        Notification notification = notificationRepository.findById(id).orElse(null);
        if (notification == null || !notification.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Notification not found.");
        }
        notification.setRead(true);
        notificationRepository.save(notification);
        return ResponseEntity.ok("Notification marked as read.");
    }

    @PostMapping("/mark-all-read")
    public ResponseEntity<?> markAllNotificationsAsRead(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        List<Notification> unread = notificationRepository.findByUserIdAndReadFalseOrderByTimestampDesc(userId);
        for (Notification n : unread) {
            n.setRead(true);
        }
        notificationRepository.saveAll(unread);
        return ResponseEntity.ok("All notifications marked as read.");
    }

    private NotificationDTO convertToDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setMessage(notification.getMessage());
        dto.setType(notification.getType());
        dto.setLink(notification.getLink());
        dto.setTimestamp(notification.getTimestamp());
        return dto;
    }

    private Long getUserIdFromAuthentication(Authentication authentication) {
        return Long.parseLong(authentication.getName());
    }
}
