package com.plr.aduaja.service;

import com.plr.aduaja.model.Notification;
import java.util.List;

// ============================================================
// ABSTRACTION (Abstraksi): NotificationService Interface sebagai kontrak
// Controller hanya tahu interface ini, tidak tahu implementasinya
//
// POLYMORPHISM (Compile-time / Overloading):
// - getNotificationsByUser(userId)
// - getUnreadNotificationsByUser(userId)         ← Overload
// - getNotificationsByType(userId, type)         ← Overload
// ============================================================
public interface NotificationService {

    // OVERLOADING: method sama, parameter beda
    List<Notification> getNotificationsByUser(String userId);
    List<Notification> getUnreadNotificationsByUser(String userId);       // OVERLOAD
    List<Notification> getNotificationsByType(String userId, String referenceType);  // OVERLOAD

    Notification createNotification(String userId, String title, String message,
                                     String referenceType, String referenceId);
    Notification markAsRead(String notificationId);
    int markAllAsReadByUser(String userId);
    long countUnreadByUser(String userId);

    // ===========================
    // BACKWARD COMPATIBILITY (untuk WebController / service lama)
    // ===========================
    default List<Notification> getUnreadNotifications(String userId) {
        return getUnreadNotificationsByUser(userId);
    }

    default long getUnreadCount(String userId) {
        return countUnreadByUser(userId);
    }

    default void markAllAsRead(String userId) {
        markAllAsReadByUser(userId);
    }

    default Notification createNotification(String userId, String message,
                                             Notification.NotificationType type) {
        return createNotification(userId,
                type != null ? type.name() : "NOTIFIKASI",
                message, "SYSTEM", null);
    }

    default Notification createNotificationForReport(String userId,
                                                      com.plr.aduaja.model.Report report,
                                                      String message,
                                                      Notification.NotificationType type) {
        return createNotification(userId,
                type != null ? type.name() : "NOTIFIKASI",
                message, "REPORT",
                report != null ? report.getReportId() : null);
    }
}
