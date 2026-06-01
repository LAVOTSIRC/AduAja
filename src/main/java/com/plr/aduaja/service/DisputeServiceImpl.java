package com.plr.aduaja.service;

import com.plr.aduaja.dto.DisputeDTO;
import com.plr.aduaja.model.ConfirmationRequest;
import com.plr.aduaja.model.DisputeRecord;
import com.plr.aduaja.model.DisputeRecord.ResolutionType;
import com.plr.aduaja.model.Report;
import com.plr.aduaja.model.User;
import com.plr.aduaja.repository.ConfirmationRequestRepository;
import com.plr.aduaja.repository.DisputeRecordRepository;
import com.plr.aduaja.repository.ReportRepository;
import com.plr.aduaja.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// ============================================================
// POLYMORPHISM — @Override (Run-time Polymorphism)
// ============================================================
@Service
public class DisputeServiceImpl implements DisputeService {

    @Autowired
    private DisputeRecordRepository disputeRecordRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConfirmationRequestRepository confirmationRequestRepository;

    @Autowired
    private ReportService reportService;

    @Override  // ← POLYMORPHISM: Override dari interface
    @Transactional
    public DisputeRecord createDispute(DisputeDTO dto, String disputantId) {
        // FR-RSL-10: Validasi alasan sengketa wajib diisi
        if (dto.getReason() == null || dto.getReason().isBlank()) {
            throw new IllegalArgumentException("Alasan sengketa wajib diisi.");
        }
        // FR-RSL-10: Validasi foto bukti wajib dilampirkan
        if (dto.getEvidencePhotoUrl() == null || dto.getEvidencePhotoUrl().isBlank()) {
            throw new IllegalArgumentException("Foto bukti sengketa wajib dilampirkan.");
        }

        Report report = reportRepository.findById(dto.getReportId())
                .orElseThrow(() -> new RuntimeException("Report tidak ditemukan: " + dto.getReportId()));
        User filedBy = userRepository.findById(disputantId)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan: " + disputantId));

        // Sengketa hanya bisa diajukan saat status MENUNGGU_VALIDASI
        if (report.getStatus() != Report.ReportStatus.MENUNGGU_VALIDASI) {
            throw new IllegalStateException("Sengketa hanya dapat diajukan saat laporan berstatus 'Menunggu Konfirmasi Warga'.");
        }

        // FR-RSL-11: Maksimal 1 sengketa per tiket
        if (disputeRecordRepository.findByReportReportId(dto.getReportId()).isPresent()) {
            throw new IllegalStateException("Sengketa untuk laporan ini sudah pernah diajukan. Maksimal 1 kali pengajuan sengketa per tiket.");
        }

        // Jika ada ConfirmationRequest yang belum dikunci, tandai sebagai TOLAK dan kunci
        confirmationRequestRepository.findByReportReportId(dto.getReportId()).ifPresent(conf -> {
            if (!Boolean.TRUE.equals(conf.getIsLocked())) {
                conf.setResponse(ConfirmationRequest.ResponseType.TOLAK);
                conf.setRespondedAt(LocalDateTime.now());
                conf.setIsLocked(true);
                confirmationRequestRepository.save(conf);
            }
        });

        DisputeRecord dispute = new DisputeRecord();
        dispute.setReport(report);
        dispute.setFiledBy(filedBy);
        dispute.setReasonText(dto.getReason());
        dispute.setEvidencePhotoUrl(dto.getEvidencePhotoUrl() != null ? dto.getEvidencePhotoUrl() : "");
        dispute.setFiledAt(LocalDateTime.now());

        Report.ReportStatus oldStatus = report.getStatus();
        report.setStatus(Report.ReportStatus.SENGKETA);
        reportRepository.save(report);
        reportService.addReportRevision(report, oldStatus, Report.ReportStatus.SENGKETA,
            "Sengketa diajukan oleh warga", disputantId);

        return disputeRecordRepository.save(dispute);
    }

    @Override  // ← POLYMORPHISM: Override dari interface
    @Transactional
    public DisputeRecord resolveDispute(String disputeId, ResolutionType resolution, String adminId, String resolutionNotes) {
        DisputeRecord dispute = disputeRecordRepository.findById(disputeId)
                .orElseThrow(() -> new RuntimeException("Sengketa tidak ditemukan: " + disputeId));

        if (dispute.getResolution() != null) {
            throw new IllegalStateException("Sengketa ini sudah pernah diputus sebelumnya.");
        }

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin tidak ditemukan: " + adminId));

        dispute.setResolution(resolution);
        dispute.setResolvedBy(admin);
        dispute.setResolutionNotes(resolutionNotes);
        dispute.setResolvedAt(LocalDateTime.now());

        Report report = dispute.getReport();
        Report.ReportStatus oldStatus = report.getStatus();
        if (resolution == ResolutionType.TUGASKAN_KEMBALI) {
            report.setStatus(Report.ReportStatus.DITUGASKAN);
            reportRepository.save(report);
            reportService.addReportRevision(report, oldStatus, Report.ReportStatus.DITUGASKAN,
                resolutionNotes, adminId);
        } else {
            report.setStatus(Report.ReportStatus.SELESAI);
            reportRepository.save(report);
            reportService.addReportRevision(report, oldStatus, Report.ReportStatus.SELESAI,
                resolutionNotes, adminId);
        }

        return disputeRecordRepository.save(dispute);
    }

    @Override  // ← POLYMORPHISM: Override dari interface
    public Optional<DisputeRecord> getDisputeById(String disputeId) {
        return disputeRecordRepository.findById(disputeId);
    }

    @Override  // ← POLYMORPHISM: Override dari interface (OVERLOAD — 1 param String)
    public List<DisputeRecord> getDisputes(String reportId) {
        return disputeRecordRepository.findByReportReportId(reportId)
                .map(List::of)
                .orElse(List.of());
    }

    @Override  // ← POLYMORPHISM: Override dari interface (OVERLOAD — 1 param ResolutionType)
    public List<DisputeRecord> getDisputes(ResolutionType resolution) {
        if (resolution == null) {
            return disputeRecordRepository.findByResolutionIsNull();
        }
        return disputeRecordRepository.findByResolution(resolution);
    }

    @Override  // ← POLYMORPHISM: Override dari interface
    public List<DisputeRecord> getAllDisputes() {
        return disputeRecordRepository.findAll();
    }

    @Override
    public List<DisputeRecord> getPendingDisputes() {
        return disputeRecordRepository.findByResolutionIsNull();
    }
}
