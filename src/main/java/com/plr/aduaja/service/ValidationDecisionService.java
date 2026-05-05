package com.plr.aduaja.service;

import com.plr.aduaja.model.*;
import com.plr.aduaja.model.ValidationDecision.Decision;
import com.plr.aduaja.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ValidationDecisionService {

    @Autowired
    private ValidationDecisionRepository validationDecisionRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    public List<ValidationDecision> getDecisionsByReport(String reportId) {
        return validationDecisionRepository.findByReportReportIdOrderByDecidedAtDesc(reportId);
    }

    public ValidationDecision createDecision(String reportId, String adminId, Decision decision, String rejectionReason) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        ValidationDecision vd = new ValidationDecision();
        vd.setReport(report);
        vd.setAdmin(admin);
        vd.setDecision(decision);
        vd.setRejectionReason(rejectionReason);
        vd.setDecidedAt(LocalDateTime.now());

        if (decision == Decision.DITERIMA) {
            report.setStatus(Report.ReportStatus.DIVALIDASI);
        } else if (decision == Decision.DITOLAK) {
            report.setStatus(Report.ReportStatus.DITOLAK);
        } else if (decision == Decision.DIREVISI) {
            report.setStatus(Report.ReportStatus.PERLU_REVISI);
        }
        report.setUpdatedAt(LocalDateTime.now());
        reportRepository.save(report);

        return validationDecisionRepository.save(vd);
    }
}
