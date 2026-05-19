package com.plr.aduaja.service;

import com.plr.aduaja.dto.DisputeDTO;
import com.plr.aduaja.model.DisputeRecord;
import com.plr.aduaja.model.DisputeRecord.ResolutionType;
import com.plr.aduaja.model.Report;
import com.plr.aduaja.model.User;
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

    @Override  // ← POLYMORPHISM: Override dari interface
    @Transactional
    public DisputeRecord createDispute(DisputeDTO dto, String disputantId) {
        Report report = reportRepository.findById(dto.getReportId())
                .orElseThrow(() -> new RuntimeException("Report tidak ditemukan: " + dto.getReportId()));
        User filedBy = userRepository.findById(disputantId)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan: " + disputantId));

        DisputeRecord dispute = new DisputeRecord();
        dispute.setReport(report);
        dispute.setFiledBy(filedBy);
        dispute.setReasonText(dto.getReason());
        dispute.setEvidencePhotoUrl(dto.getEvidencePhotoUrl() != null ? dto.getEvidencePhotoUrl() : "");
        dispute.setFiledAt(LocalDateTime.now());

        report.setStatus(Report.ReportStatus.SENGKETA);
        reportRepository.save(report);

        return disputeRecordRepository.save(dispute);
    }

    @Override  // ← POLYMORPHISM: Override dari interface
    @Transactional
    public DisputeRecord resolveDispute(String disputeId, ResolutionType resolution, String adminId, String resolutionNotes) {
        DisputeRecord dispute = disputeRecordRepository.findById(disputeId)
                .orElseThrow(() -> new RuntimeException("Sengketa tidak ditemukan: " + disputeId));
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin tidak ditemukan: " + adminId));

        dispute.setResolution(resolution);
        dispute.setResolvedBy(admin);
        dispute.setResolutionNotes(resolutionNotes);
        dispute.setResolvedAt(LocalDateTime.now());

        if (resolution == ResolutionType.TUGASKAN_KEMBALI) {
            dispute.getReport().setStatus(Report.ReportStatus.DITUGASKAN);
        } else {
            dispute.getReport().setStatus(Report.ReportStatus.DITUTUP);
        }
        reportRepository.save(dispute.getReport());

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
}
