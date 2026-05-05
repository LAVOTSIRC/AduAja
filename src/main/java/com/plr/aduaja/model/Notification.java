package com.plr.aduaja.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    @Column(name = "is_read")
    private Boolean isRead = false;

    @Column(name = "related_report_id")
    private String relatedReportId;

    @Column(name = "related_ticket_id")
    private String relatedTicketId;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "read_at")
    private LocalDateTime readAt;

    public enum Type {
        LAPORAN_DITERIMA, LAPORAN_DIPROSES, LAPORAN_SELESAI, LAPORAN_DITOLAK,
        TUGAS_BARU, TUGAS_UPDATE, SENGKETA, ESKALASI, SYSTEM
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }

    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }

    public String getRelatedReportId() { return relatedReportId; }
    public void setRelatedReportId(String relatedReportId) { this.relatedReportId = relatedReportId; }

    public String getRelatedTicketId() { return relatedTicketId; }
    public void setRelatedTicketId(String relatedTicketId) { this.relatedTicketId = relatedTicketId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }
}
