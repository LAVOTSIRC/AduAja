package com.plr.aduaja.service;

import com.plr.aduaja.model.Notification;
import com.plr.aduaja.model.Notification.NotificationType;
import com.plr.aduaja.model.Report;
import com.plr.aduaja.model.User;
import com.plr.aduaja.repository.NotificationRepository;
import com.plr.aduaja.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Notification> getNotificationsByUser(String userId) {
        return notificationRepository.findByRecipientUserIdOrderBySentAtDesc(userId);
    }

    public List<Notification> getUnreadNotifications(String userId) {
        return notificationRepository.findByRecipientUserIdAndIsReadFalse(userId);
    }

    public long getUnreadCount(String userId) {
        return notificationRepository.countByRecipientUserIdAndIsReadFalse(userId);
    }

    public Notification createNotification(String userId, String message, NotificationType type) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Notification notification = new Notification();
        notification.setRecipient(user);
        notification.setMessageText(message);
        notification.setNotificationType(type);
        notification.setIsRead(false);
        notification.setSentAt(LocalDateTime.now());

        return notificationRepository.save(notification);
    }

    public Notification createNotificationForReport(String userId, Report report, String message, NotificationType type) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Notification notification = new Notification();
        notification.setRecipient(user);
        notification.setReport(report);
        notification.setMessageText(message);
        notification.setNotificationType(type);
        notification.setIsRead(false);
        notification.setSentAt(LocalDateTime.now());

        return notificationRepository.save(notification);
    }

    public Notification markAsRead(String notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.setIsRead(true);

        return notificationRepository.save(notification);
    }

    public void markAllAsRead(String userId) {
        List<Notification> unread = notificationRepository.findByRecipientUserIdAndIsReadFalse(userId);
        for (Notification notification : unread) {
            notification.setIsRead(true);
        }
        notificationRepository.saveAll(unread);
    }
}
