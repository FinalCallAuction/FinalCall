// src/main/java/com/finalcall/auctionservice/repository/NotificationRepository.java

package com.finalcall.auctionservice.repository;

import com.finalcall.auctionservice.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Notification entity.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Retrieves all notifications for a specific user.
     *
     * @param userId The ID of the user.
     * @return List of notifications belonging to the user.
     */
    List<Notification> findByUserIdOrderByTimestampDesc(Long userId);

    /**
     * Retrieves all unread notifications for a specific user.
     *
     * @param userId The ID of the user.
     * @return List of unread notifications.
     */
    List<Notification> findByUserIdAndReadFalseOrderByTimestampDesc(Long userId);
}
