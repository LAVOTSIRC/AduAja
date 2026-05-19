package com.plr.aduaja.service;

import com.plr.aduaja.model.AuditLog;
import com.plr.aduaja.model.AuditLogFactory;
import com.plr.aduaja.model.Report;
import com.plr.aduaja.model.User;
import com.plr.aduaja.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// ============================================================
// POLYMORPHISM — @Override (Run-time Polymorphism)
// ENCAPSULATION — AuditLog setter package-private, diakses via AuditLogFactory
// AuditLogServiceImpl tidak langsung memanggil setter AuditLog;
// melainkan mendelegasikan ke AuditLogFactory (dalam package model)
// ============================================================
@Service
public class AuditLogServiceImpl implements AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Override  // ← POLYMORPHISM: Override dari interface (4 parameter)
    @Transactional
    public AuditLog log(User actor, String actionType, String oldVal, String newVal) {
        // Delegasikan pembuatan ke AuditLogFactory (package model) — ENCAPSULATION terjaga
        AuditLog auditLog = AuditLogFactory.create(actor, actionType, oldVal, newVal);
        return auditLogRepository.save(auditLog);
    }

    @Override  // ← POLYMORPHISM: Override dari interface (5 parameter — OVERLOAD)
    @Transactional
    public AuditLog log(User actor, Report report, String action, String oldVal, String newVal) {
        AuditLog auditLog = AuditLogFactory.createWithReport(actor, report, action, oldVal, newVal);
        return auditLogRepository.save(auditLog);
    }

    @Override  // ← POLYMORPHISM: Override dari interface (8 parameter — OVERLOAD)
    @Transactional
    public AuditLog log(User actor, String targetType, String targetId, String action,
                        String oldVal, String newVal, String ipAddress, String deviceInfo) {
        // Fix 3: targetType + targetId menggantikan FieldTask (tidak bergantung modul lain)
        AuditLog auditLog = AuditLogFactory.createFull(
                actor, targetType, targetId, action, oldVal, newVal, ipAddress, deviceInfo);
        return auditLogRepository.save(auditLog);
    }

    @Override  // ← POLYMORPHISM: Override dari interface
    public List<AuditLog> getLogsByReport(String reportId) {
        return auditLogRepository.findByReportReportIdOrderByLoggedAtDesc(reportId);
    }

    @Override  // ← POLYMORPHISM: Override dari interface
    public List<AuditLog> getLogsByActor(String actorId) {
        return auditLogRepository.findByActorUserId(actorId);
    }

    // Fix 3: Ganti getLogsByTask → getLogsByTarget(targetType, targetId)
    @Override  // ← POLYMORPHISM: Override dari interface
    public List<AuditLog> getLogsByTarget(String targetType, String targetId) {
        return auditLogRepository.findByTargetTypeAndTargetId(targetType, targetId);
    }

    @Override  // ← POLYMORPHISM: Override dari interface
    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAll();
    }
}
