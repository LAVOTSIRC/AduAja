package com.plr.aduaja.service;

import com.plr.aduaja.model.Notification;
import com.plr.aduaja.model.User;
import com.plr.aduaja.repository.NotificationRepository;
import com.plr.aduaja.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

// ============================================================
// POLYMORPHISM (Run-time): NotificationServiceImpl implements NotificationService
// Setiap method @Override = POLYMORPHISM sejati
// ABSTRACTION: Controller hanya tahu interface NotificationService
// ============================================================
@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {  // ← POLYMORPHISM

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Override  // ← POLYMORPHISM
    public List<Notification> getNotificationsByUser(String userId) {
        return notificationRepository.findByRecipientUserIdOrderBySentAtDesc(userId);
    }

    @Override  // ← POLYMORPHISM (Overload)
    public List<Notification> getUnreadNotificationsByUser(String userId) {
        return notificationRepository.findByRecipientUserIdAndIsReadFalse(userId);
    }

    @Override  // ← POLYMORPHISM (Overload: 2 parameter beda)
    public List<Notification> getNotificationsByType(String userId, String referenceType) {
        // Pakai query berdasarkan userId dan referenceType
        return notificationRepository.findByRecipientUserIdOrderBySentAtDesc(userId).stream()
            .filter(n -> referenceType.equals(n.getReferenceType()))
            .toList();
    }

    @Override  // ← POLYMORPHISM
    public Notification createNotification(String userId, String title, String message,
                                            String referenceType, String referenceId) {
        // ABSTRACTION: Caller tidak perlu tahu detail pembuatan notifikasi
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

        Notification notif = new Notification();
        notif.setRecipient(user);
        notif.setTitle(title);
        notif.setMessageText(message);
        notif.setReferenceType(referenceType);
        notif.setReferenceId(referenceId);
        notif.setIsRead(false);
        notif.setSentAt(LocalDateTime.now());

        return notificationRepository.save(notif);
    }

    @Override  // ← POLYMORPHISM
    public Notification markAsRead(String notificationId) {
        Notification notif = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new RuntimeException("Notifikasi tidak ditemukan"));
        notif.setIsRead(true);
        return notificationRepository.save(notif);
    }

    @Override  // ← POLYMORPHISM
    public int markAllAsReadByUser(String userId) {
        return notificationRepository.markAllAsReadByUserId(userId);
    }

    @Override  // ← POLYMORPHISM
    public long countUnreadByUser(String userId) {
        return notificationRepository.countByRecipientUserIdAndIsReadFalse(userId);
    }
}
