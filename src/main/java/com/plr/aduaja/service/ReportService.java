package com.plr.aduaja.service;

import com.plr.aduaja.model.*;
import com.plr.aduaja.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReportCategoryRepository categoryRepository;

    @Autowired
    private RegionRepository regionRepository;

    private final AtomicInteger ticketCounter = new AtomicInteger(1);

    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    public List<Report> getReportsByStatus(Report.ReportStatus status) {
        return reportRepository.findByStatusOrderBySubmittedAtDesc(status);
    }

    public List<Report> getReportsByUser(String userId) {
        return reportRepository.findByReporterUserId(userId);
    }

    public Optional<Report> getReportById(String id) {
        return reportRepository.findById(id);
    }

    public Optional<Report> getReportByTicketNumber(String ticketNumber) {
        return reportRepository.findByTicketNumber(ticketNumber);
    }

    public Report createReport(Report report, String userId) {
        User reporter = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        report.setReporter(reporter);
        report.setSubmittedAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());
        report.setStatus(Report.ReportStatus.MENUNGGU_VALIDASI);
        report.setTicketNumber(generateTicketNumber());
        return reportRepository.save(report);
    }

    public Report updateStatus(String id, Report.ReportStatus status) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        report.setStatus(status);
        report.setUpdatedAt(LocalDateTime.now());
        return reportRepository.save(report);
    }

    public long countByStatus(Report.ReportStatus status) {
        return reportRepository.countByStatus(status);
    }

    public List<Report> searchReports(String query) {
        return reportRepository.findByDescriptionContainingIgnoreCase(query);
    }

    public List<Report> getReportsForDisposisi() {
        return reportRepository.findByStatus(Report.ReportStatus.DIVALIDASI);
    }

    public List<Report> getReportsByRegion(String regionId) {
        return reportRepository.findByRegionRegionId(regionId);
    }

    public List<Report> getReportsByCategory(String categoryId) {
        return reportRepository.findByCategoryCategoryId(categoryId);
    }

    private String generateTicketNumber() {
        String year = String.valueOf(LocalDateTime.now().getYear());
        int number = ticketCounter.getAndIncrement();
        return String.format("ADJ-%s-%05d", year, number);
    }
}
