package com.plr.aduaja.service;

import com.plr.aduaja.model.*;
import com.plr.aduaja.model.DisputeRecord.ResolutionType;
import com.plr.aduaja.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DisputeService {

    @Autowired
    private DisputeRecordRepository disputeRecordRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    public List<DisputeRecord> getAllDisputes() {
        return disputeRecordRepository.findAll();
    }

    public Optional<DisputeRecord> getDisputeByReportId(String reportId) {
        return disputeRecordRepository.findByReportReportId(reportId);
    }

    public List<DisputeRecord> getPendingDisputes() {
        return disputeRecordRepository.findByResolutionIsNull();
    }

    public DisputeRecord createDispute(String reportId, String filedById, String reasonText, String evidencePhotoUrl) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        User filedBy = userRepository.findById(filedById)
                .orElseThrow(() -> new RuntimeException("User not found"));

        DisputeRecord dispute = new DisputeRecord();
        dispute.setReport(report);
        dispute.setFiledBy(filedBy);
        dispute.setReasonText(reasonText);
        dispute.setEvidencePhotoUrl(evidencePhotoUrl);
        dispute.setFiledAt(LocalDateTime.now());

        report.setStatus(Report.ReportStatus.SENGKETA);
        report.setUpdatedAt(LocalDateTime.now());
        reportRepository.save(report);

        return disputeRecordRepository.save(dispute);
    }

    public DisputeRecord resolveDispute(String disputeId, String resolvedById, ResolutionType resolution, String resolutionNotes) {
        DisputeRecord dispute = disputeRecordRepository.findById(disputeId)
                .orElseThrow(() -> new RuntimeException("Dispute not found"));
        User resolvedBy = userRepository.findById(resolvedById)
                .orElseThrow(() -> new RuntimeException("Resolver not found"));

        dispute.setResolvedBy(resolvedBy);
        dispute.setResolution(resolution);
        dispute.setResolutionNotes(resolutionNotes);
        dispute.setResolvedAt(LocalDateTime.now());

        if (resolution == ResolutionType.TUGASKAN_KEMBALI) {
            dispute.getReport().setStatus(Report.ReportStatus.DITUGASKAN);
        } else {
            dispute.getReport().setStatus(Report.ReportStatus.DITUTUP);
        }
        dispute.getReport().setUpdatedAt(LocalDateTime.now());
        reportRepository.save(dispute.getReport());

        return disputeRecordRepository.save(dispute);
    }
}
