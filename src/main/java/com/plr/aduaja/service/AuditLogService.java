package com.plr.aduaja.service;

import com.plr.aduaja.model.AuditLog;
import com.plr.aduaja.model.Report;
import com.plr.aduaja.model.User;

import java.util.List;

// ============================================================
// ABSTRACTION — Interface kontrak untuk AuditLogService
// ============================================================
public interface AuditLogService {

    // ============ OVERLOADING: nama method SAMA, parameter BERBEDA ============ //
    // POLYMORPHISM (Compile-time) — Overloading
    AuditLog log(User actor, String actionType, String oldVal, String newVal);                                           // 4 param
    AuditLog log(User actor, Report report, String action, String oldVal, String newVal);                                // 5 param — OVERLOAD
    AuditLog log(User actor, String targetType, String targetId, String action,
                 String oldVal, String newVal, String ipAddress, String deviceInfo);                                     // 8 param — OVERLOAD

    List<AuditLog> getLogsByReport(String reportId);
    List<AuditLog> getLogsByActor(String actorId);
    // Fix 3: Ganti getLogsByTask → getLogsByTarget (pakai targetType + targetId)
    List<AuditLog> getLogsByTarget(String targetType, String targetId);
    List<AuditLog> getAllLogs();
}
