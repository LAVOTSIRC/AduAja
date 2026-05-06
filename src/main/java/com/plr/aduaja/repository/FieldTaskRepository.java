package com.plr.aduaja.repository;

import com.plr.aduaja.model.FieldTask;
import com.plr.aduaja.model.FieldTask.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FieldTaskRepository extends JpaRepository<FieldTask, String> {

    List<FieldTask> findByOfficerUserId(String officerId);

    List<FieldTask> findByOfficerUserIdAndTaskStatus(String officerId, TaskStatus taskStatus);

    List<FieldTask> findByReportReportId(String reportId);

    List<FieldTask> findByAssignedByUserId(String assignedByUserId);

    long countByTaskStatus(TaskStatus taskStatus);

    Optional<FieldTask> findTopByReportReportIdOrderByStartedAtDesc(String reportId);
}
