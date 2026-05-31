package com.plr.aduaja.service;

import com.plr.aduaja.model.*;
import com.plr.aduaja.model.FieldTask.TaskStatus;
import com.plr.aduaja.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    @Autowired
    private UserProfileRepository userProfileRepository;

    private static final Logger log = LoggerFactory.getLogger(FieldTaskServiceImpl.class);

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

        FieldTask saved = fieldTaskRepository.save(task);

        // FR-PRS-03: Validasi wilayah tugas petugas vs lokasi laporan
        try {
            UserProfile profile = userProfileRepository.findByUserUserId(officerId).orElse(null);
            if (profile != null && profile.getWilayahTugas() != null && report.getLocationHint() != null) {
                String wilayahPetugas = profile.getWilayahTugas().getRegionName().toLowerCase();
                String lokasiLaporan = report.getLocationHint().toLowerCase();
                if (!lokasiLaporan.contains(wilayahPetugas) && !wilayahPetugas.contains(lokasiLaporan)) {
                    log.warn("FR-PRS-03: Wilayah tugas petugas '{}' tidak sesuai dengan lokasi laporan '{}'",
                            profile.getWilayahTugas().getRegionName(), report.getLocationHint());
                }
            }
        } catch (Exception e) {
            log.warn("FR-PRS-03: Gagal validasi wilayah: {}", e.getMessage());
        }

        return saved;
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
    public FieldTask postponeTask(String taskId, String reason) {
        FieldTask task = fieldTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        task.setTaskStatus(TaskStatus.TERTUNDA);
        fieldTaskRepository.save(task);

        // FIX: simpan record penundaan ke tabel task_postponements (Encapsulation — data terbungkus di entity)
        TaskPostponement postponement = new TaskPostponement();
        postponement.setTask(task);
        postponement.setReason(reason != null && !reason.isBlank() ? reason : "Ditunda oleh petugas");
        postponement.setRequestedAt(LocalDateTime.now());
        postponement.setApprovalStatus(TaskPostponement.ApprovalStatus.MENUNGGU);
        taskPostponementRepository.save(postponement);  // ← SEKARANG TERSIMPAN ke DB

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
}
