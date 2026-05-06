package com.plr.aduaja.service;

import com.plr.aduaja.model.*;
import com.plr.aduaja.model.FieldTask.TaskStatus;
import com.plr.aduaja.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FieldTaskService {

    @Autowired
    private FieldTaskRepository fieldTaskRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SlaRecordRepository slaRecordRepository;

    public List<FieldTask> getAllTasks() {
        return fieldTaskRepository.findAll();
    }

    public Optional<FieldTask> getTaskById(String taskId) {
        return fieldTaskRepository.findById(taskId);
    }

    public List<FieldTask> getTasksByOfficer(String officerId) {
        return fieldTaskRepository.findByOfficerUserId(officerId);
    }

    public List<FieldTask> getTasksByOfficerAndStatus(String officerId, TaskStatus status) {
        return fieldTaskRepository.findByOfficerUserIdAndTaskStatus(officerId, status);
    }

    public List<FieldTask> getTasksByReport(String reportId) {
        return fieldTaskRepository.findByReportReportId(reportId);
    }

    public FieldTask createTask(String reportId, String officerId, String assignedById) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        User officer = userRepository.findById(officerId)
                .orElseThrow(() -> new RuntimeException("Officer not found"));
        User assignedBy = userRepository.findById(assignedById)
                .orElseThrow(() -> new RuntimeException("Assigner not found"));

        FieldTask task = new FieldTask();
        task.setReport(report);
        task.setOfficer(officer);
        task.setAssignedBy(assignedBy);
        task.setTaskStatus(TaskStatus.BARU);

        SlaRecord sla = slaRecordRepository.findByReportReportId(reportId).orElse(null);
        task.setSlaRecord(sla);

        return fieldTaskRepository.save(task);
    }

    public FieldTask startTask(String taskId, BigDecimal latitude, BigDecimal longitude) {
        FieldTask task = fieldTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        task.setTaskStatus(TaskStatus.SEDANG_DIKERJAKAN);
        task.setStartedAt(LocalDateTime.now());
        task.setOfficerLatitude(latitude);
        task.setOfficerLongitude(longitude);
        return fieldTaskRepository.save(task);
    }

    public FieldTask completeTask(String taskId) {
        FieldTask task = fieldTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        task.setTaskStatus(TaskStatus.SELESAI);
        task.setCompletedAt(LocalDateTime.now());
        return fieldTaskRepository.save(task);
    }

    public long countByStatus(TaskStatus status) {
        return fieldTaskRepository.countByTaskStatus(status);
    }
}
