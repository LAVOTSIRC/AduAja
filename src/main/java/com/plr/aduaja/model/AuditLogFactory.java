package com.plr.aduaja.model;

import java.time.LocalDateTime;

// ============================================================
// FACTORY CLASS — berada dalam package com.plr.aduaja.model
// Sehingga bisa mengakses package-private setter AuditLog
// ENCAPSULATION + Immutability: hanya factory ini yang bisa buat AuditLog
// ============================================================
public class AuditLogFactory {

    // Factory method 1: basic log (tanpa report, tanpa target)
    public static AuditLog create(User actor, String actionType, String oldVal, String newVal) {
        AuditLog log = new AuditLog();
        log.setActor(actor);
        log.setActionType(actionType);
        log.setOldValue(oldVal);
        log.setNewValue(newVal);
        log.setIpAddress("0.0.0.0");
        log.setDeviceInfo("System");
        log.setLoggedAt(LocalDateTime.now());
        return log;
    }

    // Factory method 2: log dengan report (OVERLOAD via factory method naming)
    public static AuditLog createWithReport(User actor, Report report, String action, String oldVal, String newVal) {
        AuditLog log = new AuditLog();
        log.setActor(actor);
        log.setReport(report);
        log.setTargetType("REPORT");
        log.setTargetId(report != null ? report.getReportId() : null);
        log.setActionType(action);
        log.setOldValue(oldVal);
        log.setNewValue(newVal);
        log.setIpAddress("0.0.0.0");
        log.setDeviceInfo("System");
        log.setLoggedAt(LocalDateTime.now());
        return log;
    }

    // Factory method 3: log dengan targetType + targetId (paling lengkap)
    public static AuditLog createFull(User actor, String targetType, String targetId,
                                      String action, String oldVal, String newVal,
                                      String ipAddress, String deviceInfo) {
        AuditLog log = new AuditLog();
        log.setActor(actor);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setActionType(action);
        log.setOldValue(oldVal);
        log.setNewValue(newVal);
        log.setIpAddress(ipAddress);
        log.setDeviceInfo(deviceInfo);
        log.setLoggedAt(LocalDateTime.now());
        return log;
    }
}
