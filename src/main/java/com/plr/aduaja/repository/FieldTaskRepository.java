package com.plr.aduaja.repository;

import com.plr.aduaja.model.FieldTask;
import com.plr.aduaja.model.FieldTask.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FieldTaskRepository extends JpaRepository<FieldTask, String> {

    List<FieldTask> findByOfficerUserId(String officerId);

    List<FieldTask> findByOfficerUserIdAndTaskStatus(String officerId, TaskStatus taskStatus);

    List<FieldTask> findByTaskStatus(TaskStatus taskStatus);

    List<FieldTask> findByReportReportId(String reportId);

    List<FieldTask> findByAssignedByUserId(String assignedByUserId);

    List<FieldTask> findByStartedAtBetween(LocalDateTime start, LocalDateTime end);

    long countByTaskStatus(TaskStatus taskStatus);

    Optional<FieldTask> findTopByReportReportIdOrderByStartedAtDesc(String reportId);
}
