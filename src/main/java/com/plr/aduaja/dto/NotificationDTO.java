package com.plr.aduaja.dto;

import java.time.LocalDateTime;

// ============================================================
// ENCAPSULATION (Enkapsulasi): DTO memisahkan data notifikasi dari Entity
// Semua field PRIVATE — Controller tidak langsung pakai Entity
// ============================================================
public class NotificationDTO {

    // ENKAPSULASI: semua field PRIVATE
    private String notificationId;
    private String title;
    private String message;
    private String referenceType;
    private String referenceId;
    private Boolean isRead;
    private LocalDateTime sentAt;

    // ENKAPSULASI: Hanya getter & setter
    public String getNotificationId() { return notificationId; }
    public void setNotificationId(String notificationId) { this.notificationId = notificationId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getReferenceType() { return referenceType; }
    public void setReferenceType(String referenceType) { this.referenceType = referenceType; }

    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }

    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
}
