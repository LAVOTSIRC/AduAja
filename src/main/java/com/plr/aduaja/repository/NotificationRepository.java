package com.plr.aduaja.repository;

import com.plr.aduaja.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {

    List<Notification> findByRecipientUserId(String userId);

    List<Notification> findByRecipientUserIdAndIsReadFalse(String userId);

    List<Notification> findByRecipientUserIdAndNotificationType(String userId, Notification.NotificationType type);

    List<Notification> findByRecipientUserIdOrderBySentAtDesc(String userId);

    long countByRecipientUserIdAndIsReadFalse(String userId);

    long countByRecipientUserId(String userId);

    @Query("SELECT n FROM Notification n WHERE n.recipient.id = :userId ORDER BY n.sentAt DESC")
    List<Notification> findRecentByUserId(@Param("userId") String userId);
}
