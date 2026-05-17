package com.plr.aduaja.service;

import com.plr.aduaja.model.FieldTask;
import com.plr.aduaja.model.FieldTask.TaskStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FieldTaskService {

    List<FieldTask> getAllTasks();

    Optional<FieldTask> getTaskById(String taskId);

    List<FieldTask> getTasksByOfficer(String officerId);

    List<FieldTask> getTasksByOfficerAndStatus(String officerId, TaskStatus status);

    List<FieldTask> getTasksByStatus(TaskStatus status);

    List<FieldTask> getTasksByReport(String reportId);

    List<FieldTask> getTasksByDateRange(LocalDateTime start, LocalDateTime end);

    FieldTask createTask(String reportId, String officerId, String assignedById);

    FieldTask startTask(String taskId, BigDecimal latitude, BigDecimal longitude);

    FieldTask completeTask(String taskId);

    FieldTask completeTask(String taskId, String evidencePhotoUrl);

    FieldTask postponeTask(String taskId, String reason);

    FieldTask reassignTask(String taskId, String newOfficerId);

    long countByStatus(TaskStatus status);
}
