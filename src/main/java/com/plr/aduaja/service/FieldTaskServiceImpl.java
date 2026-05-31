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
public class FieldTaskServiceImpl implements FieldTaskService {

    @Autowired
    private FieldTaskRepository fieldTaskRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SlaRecordRepository slaRecordRepository;

    @Autowired
    private TaskEvidenceRepository taskEvidenceRepository;

    @Autowired
    private TaskPostponementRepository taskPostponementRepository;  // FIX: inject repo untuk simpan record penundaan

    @Autowired
    private ConfirmationRequestRepository confirmationRequestRepository;  // FIX: untuk buat ConfirmationRequest saat task selesai

    @Override
    public List<FieldTask> getAllTasks() {
        return fieldTaskRepository.findAll();
    }

    @Override
    public Optional<FieldTask> getTaskById(String taskId) {
        return fieldTaskRepository.findById(taskId);
    }

    @Override
    public List<FieldTask> getTasksByOfficer(String officerId) {
        return fieldTaskRepository.findByOfficerUserId(officerId);
    }

    @Override
    public List<FieldTask> getTasksByOfficerAndStatus(String officerId, TaskStatus status) {
        return fieldTaskRepository.findByOfficerUserIdAndTaskStatus(officerId, status);
    }

    @Override
    public List<FieldTask> getTasksByStatus(TaskStatus status) {
        return fieldTaskRepository.findByTaskStatus(status);
    }

    @Override
    public List<FieldTask> getTasksByReport(String reportId) {
        return fieldTaskRepository.findByReportReportId(reportId);
    }

    @Override
    public List<FieldTask> getTasksByDateRange(LocalDateTime start, LocalDateTime end) {
        return fieldTaskRepository.findByStartedAtBetween(start, end);
    }

    @Override
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

    @Override
    public FieldTask startTask(String taskId, BigDecimal latitude, BigDecimal longitude) {
        FieldTask task = fieldTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        task.setTaskStatus(TaskStatus.SEDANG_DIKERJAKAN);
        task.setStartedAt(LocalDateTime.now());
        task.setOfficerLatitude(latitude);
        task.setOfficerLongitude(longitude);
        return fieldTaskRepository.save(task);
    }

    @Override
    public FieldTask completeTask(String taskId) {
        return completeTask(taskId, null);
    }

    @Override
    public FieldTask completeTask(String taskId, String evidencePhotoUrl) {
        FieldTask task = fieldTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        task.setTaskStatus(TaskStatus.SELESAI);
        task.setCompletedAt(LocalDateTime.now());
        fieldTaskRepository.save(task);

        if (evidencePhotoUrl != null && !evidencePhotoUrl.isBlank()) {
            TaskEvidence evidence = new TaskEvidence();
            evidence.setTask(task);
            evidence.setEvidenceType(TaskEvidence.EvidenceType.SESUDAH);
            evidence.setPhotoUrl(evidencePhotoUrl);
            evidence.setTakenAt(LocalDateTime.now());
            taskEvidenceRepository.save(evidence);
        }

        // CRITICAL FIX: Update status laporan ke MENUNGGU_KONFIRMASI (SRS Flow 5.5)
        // dan buat ConfirmationRequest dengan deadline 72 jam
        Report report = task.getReport();
        if (report != null) {
            report.setStatus(Report.ReportStatus.MENUNGGU_KONFIRMASI);
            reportRepository.save(report);

            // Buat ConfirmationRequest jika belum ada
            boolean alreadyExists = confirmationRequestRepository
                    .findByReportReportId(report.getReportId()).isPresent();
            if (!alreadyExists && report.getReporter() != null) {
                ConfirmationRequest confirmation = new ConfirmationRequest();
                confirmation.setReport(report);
                confirmation.setWarga(report.getReporter());
                confirmation.setDeadlineAt(LocalDateTime.now().plusHours(72)); // 3x24 jam
                confirmation.setIsLocked(false);
                confirmationRequestRepository.save(confirmation);
            }
        }

        return task;
    }

    @Override
    public FieldTask postponeTask(String taskId, String reason, String requestedById) {
        FieldTask task = fieldTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        User requestedBy = requestedById != null ? userRepository.findById(requestedById).orElse(null) : null;
        task.setTaskStatus(TaskStatus.TERTUNDA);
        fieldTaskRepository.save(task);

        TaskPostponement postponement = new TaskPostponement();
        postponement.setTask(task);
        postponement.setRequestedBy(requestedBy);
        postponement.setReason(reason != null && !reason.isBlank() ? reason : "Ditunda oleh petugas");
        postponement.setRequestedAt(LocalDateTime.now());
        postponement.setApprovalStatus(TaskPostponement.ApprovalStatus.MENUNGGU);
        taskPostponementRepository.save(postponement);

        return task;
    }

    @Override
    public FieldTask reassignTask(String taskId, String newOfficerId) {
        FieldTask task = fieldTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        User newOfficer = userRepository.findById(newOfficerId)
                .orElseThrow(() -> new RuntimeException("New officer not found"));
        task.setOfficer(newOfficer);
        task.setTaskStatus(TaskStatus.DITUGASKAN_ULANG);
        return fieldTaskRepository.save(task);
    }

    @Override
    public long countByStatus(TaskStatus status) {
        return fieldTaskRepository.countByTaskStatus(status);
    }

    @Override
    public Optional<TaskPostponement> getLatestPostponement(String taskId) {
        List<TaskPostponement> list = taskPostponementRepository.findByTaskTaskIdOrderByRequestedAtDesc(taskId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public List<TaskEvidence> getEvidencesByTaskAndType(String taskId, TaskEvidence.EvidenceType type) {
        return taskEvidenceRepository.findByTaskTaskIdAndEvidenceType(taskId, type);
    }

    @Override
    public FieldTask closeTaskByAdmin(String taskId) {
        FieldTask task = fieldTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        task.setTaskStatus(TaskStatus.SELESAI);
        task.setCompletedAt(LocalDateTime.now());
        fieldTaskRepository.save(task);

        Report report = task.getReport();
        if (report != null) {
            report.setStatus(Report.ReportStatus.SELESAI);
            reportRepository.save(report);
        }

        return task;
    }

    @Override
    public void saveTaskEvidence(String taskId, String photoUrl, TaskEvidence.EvidenceType type) {
        FieldTask task = fieldTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        TaskEvidence evidence = new TaskEvidence();
        evidence.setTask(task);
        evidence.setEvidenceType(type);
        evidence.setPhotoUrl(photoUrl);
        evidence.setTakenAt(LocalDateTime.now());
        taskEvidenceRepository.save(evidence);
    }
}
