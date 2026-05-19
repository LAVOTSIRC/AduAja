package com.plr.aduaja.service;

import com.plr.aduaja.model.Report;
import com.plr.aduaja.model.User;
import com.plr.aduaja.model.ValidationDecision;
import com.plr.aduaja.model.ValidationDecision.Decision;
import com.plr.aduaja.repository.ReportRepository;
import com.plr.aduaja.repository.UserRepository;
import com.plr.aduaja.repository.ValidationDecisionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

// ============================================================
// POLYMORPHISM — @Override (Run-time Polymorphism)
// ============================================================
@Service
public class ValidationDecisionServiceImpl implements ValidationDecisionService {

    @Autowired
    private ValidationDecisionRepository validationDecisionRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    @Override  // ← POLYMORPHISM: Override dari interface
    @Transactional
    public ValidationDecision createDecision(String reportId, String adminId, Decision decision, String reason) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report tidak ditemukan: " + reportId));
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin tidak ditemukan: " + adminId));

        ValidationDecision vd = new ValidationDecision();
        vd.setReport(report);
        vd.setAdmin(admin);
        vd.setDecision(decision);
        vd.setRejectionReason(reason);
        vd.setDecidedAt(LocalDateTime.now());

        if (decision == Decision.DITERIMA) {
            report.setStatus(Report.ReportStatus.DIVALIDASI);
        } else if (decision == Decision.DITOLAK) {
            report.setStatus(Report.ReportStatus.DITOLAK);
        } else if (decision == Decision.DIREVISI) {
            report.setStatus(Report.ReportStatus.PERLU_REVISI);
        }
        reportRepository.save(report);

        return validationDecisionRepository.save(vd);
    }

    @Override  // ← POLYMORPHISM: Override dari interface (OVERLOAD — tanpa parameter)
    public List<ValidationDecision> getDecisions() {
        return validationDecisionRepository.findAll();
    }

    @Override  // ← POLYMORPHISM: Override dari interface (OVERLOAD — 1 parameter String)
    public List<ValidationDecision> getDecisions(String reportOrAdminId) {
        List<ValidationDecision> byReport = validationDecisionRepository.findByReportReportId(reportOrAdminId);
        if (!byReport.isEmpty()) return byReport;
        return validationDecisionRepository.findByAdminUserId(reportOrAdminId);
    }
}
