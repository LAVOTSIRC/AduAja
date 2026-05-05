package com.plr.aduaja.service;

import com.plr.aduaja.model.Report;
import com.plr.aduaja.model.User;
import com.plr.aduaja.repository.ReportRepository;
import com.plr.aduaja.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    public List<Report> getReportsByStatus(Report.Status status) {
        return reportRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    public List<Report> getReportsByUser(String userId) {
        return reportRepository.findByReporterId(userId);
    }

    public Optional<Report> getReportById(String id) {
        return reportRepository.findById(id);
    }

    public Report createReport(Report report, String userId) {
        User reporter = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        report.setReporter(reporter);
        report.setCreatedAt(LocalDateTime.now());
        report.setStatus(Report.Status.MENUNGGU);
        return reportRepository.save(report);
    }

    public Report validateReport(String id, boolean approved, String rejectionReason, String validatedByUserId) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        User validator = userRepository.findById(validatedByUserId)
                .orElseThrow(() -> new RuntimeException("Validator not found"));

        report.setValidatedAt(LocalDateTime.now());
        report.setValidatedBy(validator);

        if (approved) {
            report.setStatus(Report.Status.DIVALIDASI);
        } else {
            report.setStatus(Report.Status.DITOLAK);
            report.setRejectionReason(rejectionReason);
        }

        return reportRepository.save(report);
    }

    public Report updateStatus(String id, Report.Status status) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        report.setStatus(status);
        return reportRepository.save(report);
    }

    public long countByStatus(Report.Status status) {
        return reportRepository.countByStatus(status);
    }

    public List<Report> searchReports(String query) {
        return reportRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query);
    }

    public List<Report> getReportsForDisposisi() {
        return reportRepository.findByDisposisiIsNullAndStatusIn(
                List.of(Report.Status.DIVALIDASI)
        );
    }
}
