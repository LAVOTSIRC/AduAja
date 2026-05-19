package com.plr.aduaja.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// ============================================================
// INHERITANCE (Pewarisan): Notification extends BaseEntity
// Mendapatkan createdAt dan updatedAt otomatis dari parent
// ============================================================
@Entity
@Table(name = "notifications")
public class Notification extends BaseEntity {  // ← INHERITANCE sejati

    // ENKAPSULASI: semua field PRIVATE
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "notification_id")
    private String notificationId;

    // HAS-A (ManyToOne) — bukan Inheritance
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    // HAS-A optional (laporan terkait)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id")
    private Report report;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "message_text", columnDefinition = "TEXT", nullable = false)
    private String messageText;

    @Column(name = "reference_type", length = 50)
    private String referenceType;  // "REPORT", "VALIDATION", etc.

    @Column(name = "reference_id")
    private String referenceId;   // ID laporan terkait

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type")
    private NotificationType notificationType;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt = LocalDateTime.now();

    public enum NotificationType {
        STATUS_BERUBAH, SENGKETA_MASUK, SENGKETA_DIPUTUS, TUGAS_BARU, SLA_WARNING, SELESAI_OTOMATIS,
        LAPORAN_DITERIMA, LAPORAN_DIVALIDASI, LAPORAN_DITOLAK, LAPORAN_PERLU_REVISI
    }

    // ENKAPSULASI: Hanya getter & setter
    public String getNotificationId() { return notificationId; }
    public void setNotificationId(String notificationId) { this.notificationId = notificationId; }

    public User getRecipient() { return recipient; }
    public void setRecipient(User recipient) { this.recipient = recipient; }

    // alias "user" for prompt compatibility
    public User getUser() { return recipient; }
    public void setUser(User user) { this.recipient = user; }

    public Report getReport() { return report; }
    public void setReport(Report report) { this.report = report; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessageText() { return messageText; }
    public void setMessageText(String messageText) { this.messageText = messageText; }

    // alias "message" for prompt compatibility
    public String getMessage() { return messageText; }
    public void setMessage(String message) { this.messageText = message; }

    public String getReferenceType() { return referenceType; }
    public void setReferenceType(String referenceType) { this.referenceType = referenceType; }

    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }

    public NotificationType getNotificationType() { return notificationType; }
    public void setNotificationType(NotificationType notificationType) { this.notificationType = notificationType; }

    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
}
