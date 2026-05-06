package com.plr.aduaja.repository;

import com.plr.aduaja.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, String> {

    List<AuditLog> findByReportReportId(String reportId);

    List<AuditLog> findByTaskTaskId(String taskId);

    List<AuditLog> findByActorUserId(String actorId);

    List<AuditLog> findByReportReportIdOrderByLoggedAtDesc(String reportId);

    List<AuditLog> findByActionType(String actionType);
}
