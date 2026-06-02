package com.plr.aduaja.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "log_id")
    private String logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private User actor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id")
    private Report report;

    // Fix 2: Ganti FieldTask task → targetType + targetId (lebih fleksibel, tidak bergantung modul lain)
    @Column(name = "target_type")
    private String targetType;

    @Column(name = "target_id")
    private String targetId;

    @Column(name = "action_type", nullable = false, length = 100)
    private String actionType;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "device_info", length = 255)
    private String deviceInfo;

    @Column(name = "logged_at", nullable = false)
    private LocalDateTime loggedAt = LocalDateTime.now();

    // ============ GETTER (PUBLIC) ============ //
    public String getLogId() { return logId; }
    public User getActor() { return actor; }
    public Report getReport() { return report; }
    public String getTargetType() { return targetType; }
    public String getTargetId() { return targetId; }
    public String getActionType() { return actionType; }
    public String getOldValue() { return oldValue; }
    public String getNewValue() { return newValue; }
    public String getIpAddress() { return ipAddress; }
    public String getDeviceInfo() { return deviceInfo; }
    public LocalDateTime getLoggedAt() { return loggedAt; }

    // ============ PACKAGE-PRIVATE SETTER (IMMUTABLE — ENCAPSULATION) ============ //
    // Fix 1: Setter package-private (tanpa 'public') → hanya bisa diakses dalam package com.plr.aduaja.model
    void setLogId(String logId) { this.logId = logId; }
    void setActor(User actor) { this.actor = actor; }
    void setReport(Report report) { this.report = report; }
    void setTargetType(String targetType) { this.targetType = targetType; }
    void setTargetId(String targetId) { this.targetId = targetId; }
    void setActionType(String actionType) { this.actionType = actionType; }
    void setOldValue(String oldValue) { this.oldValue = oldValue; }
    void setNewValue(String newValue) { this.newValue = newValue; }
    void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    void setDeviceInfo(String deviceInfo) { this.deviceInfo = deviceInfo; }
    void setLoggedAt(LocalDateTime loggedAt) { this.loggedAt = loggedAt; }
    // ======================================================
    // Static factory method (Builder Pattern) — agar class
    // luar package tetap bisa membuat AuditLog tanpa setter publik.
    // Setter tetap package-private untuk menjaga immutability.
    // ======================================================
    public static AuditLog create(User actor, Report report,
                                   String targetType, String targetId,
                                   String actionType, String oldValue, String newValue) {
        AuditLog log = new AuditLog();
        log.setActor(actor);
        log.setReport(report);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setActionType(actionType);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        return log;
    }
}
