package com.plr.aduaja.service;

import com.plr.aduaja.model.AuditLog;
import com.plr.aduaja.model.FieldTask;
import com.plr.aduaja.model.Report;
import com.plr.aduaja.model.User;
import com.plr.aduaja.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    public List<AuditLog> getLogsByReport(String reportId) {
        return auditLogRepository.findByReportReportIdOrderByLoggedAtDesc(reportId);
    }

    public List<AuditLog> getLogsByTask(String taskId) {
        return auditLogRepository.findByTaskTaskId(taskId);
    }

    public List<AuditLog> getLogsByActor(String actorId) {
        return auditLogRepository.findByActorUserId(actorId);
    }

    public AuditLog createLog(User actor, Report report, FieldTask task, String actionType, String oldValue, String newValue, String ipAddress, String deviceInfo) {
        AuditLog log = new AuditLog();
        log.setActor(actor);
        log.setReport(report);
        log.setTask(task);
        log.setActionType(actionType);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setIpAddress(ipAddress);
        log.setDeviceInfo(deviceInfo);
        log.setLoggedAt(LocalDateTime.now());
        return auditLogRepository.save(log);
    }

    public AuditLog createLog(User actor, Report report, String actionType, String oldValue, String newValue) {
        return createLog(actor, report, null, actionType, oldValue, newValue, null, null);
    }
}
