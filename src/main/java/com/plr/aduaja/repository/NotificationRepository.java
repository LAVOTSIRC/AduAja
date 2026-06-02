package com.plr.aduaja.repository;

import com.plr.aduaja.model.Notification;
import com.plr.aduaja.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {

    // By user object (for new service pattern)
    List<Notification> findByRecipientOrderBySentAtDesc(User user);
    List<Notification> findByRecipientAndIsReadOrderBySentAtDesc(User user, Boolean isRead);
    List<Notification> findByRecipientAndReferenceTypeOrderBySentAtDesc(User user, String referenceType);

    // By userId string (backward compatible with old service)
    List<Notification> findByRecipientUserId(String userId);
    List<Notification> findByRecipientUserIdAndIsReadFalse(String userId);
    List<Notification> findByRecipientUserIdAndNotificationType(String userId, Notification.NotificationType type);
    List<Notification> findByRecipientUserIdOrderBySentAtDesc(String userId);

    long countByRecipientUserIdAndIsReadFalse(String userId);
    long countByRecipientUserId(String userId);

    // For NotificationServiceImpl
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipient.userId = :userId AND n.isRead = :isRead")
    long countByUserIdAndIsRead(@Param("userId") String userId, @Param("isRead") Boolean isRead);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipient.userId = :userId AND n.isRead = false")
    int markAllAsReadByUserId(@Param("userId") String userId);

    @Query("SELECT n FROM Notification n WHERE n.recipient.userId = :userId ORDER BY n.sentAt DESC")
    List<Notification> findRecentByUserId(@Param("userId") String userId);
}
