package com.plr.aduaja.service;

import com.plr.aduaja.model.Report;
import com.plr.aduaja.model.ReportRevision;
import com.plr.aduaja.model.ReportCategory;
import com.plr.aduaja.model.User;
import com.plr.aduaja.repository.ReportRepository;
import com.plr.aduaja.repository.ReportCategoryRepository;
import com.plr.aduaja.repository.ReportRevisionRepository;
import com.plr.aduaja.repository.UserRepository;
import com.plr.aduaja.dto.CreateReportDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// ============================================================
// POLYMORPHISM (Run-time): ReportServiceImpl implements ReportService
// Setiap method @Override = POLYMORPHISM sejati
// ABSTRACTION: Controller hanya tahu interface ReportService
// ============================================================
@Service
@Transactional
public class ReportServiceImpl implements ReportService {  // ← POLYMORPHISM

    // ABSTRACTION: hanya inject interface Repository
    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private ReportCategoryRepository categoryRepository;

    @Autowired
    private ReportRevisionRepository revisionRepository;

    @Autowired
    private UserRepository userRepository;

    // ===========================
    // @Override — Run-time Polymorphism
    // ===========================

    @Override  // ← POLYMORPHISM
    public Optional<Report> findById(String id) {
        return reportRepository.findById(id);
    }

    @Override  // ← POLYMORPHISM
    public Optional<Report> findByTicketNumber(String ticketNumber) {
        return reportRepository.findByTicketNumber(ticketNumber);
    }

    @Override  // ← POLYMORPHISM
    public List<Report> getAllReports() {
        return reportRepository.findAllByOrderBySubmittedAtDesc();
    }

    @Override  // ← POLYMORPHISM
    public List<Report> getReportsByStatus(Report.ReportStatus status) {
        return reportRepository.findByStatusOrderBySubmittedAtDesc(status);
    }

    @Override  // ← POLYMORPHISM
    public List<Report> getReportsByWarga(String wargaId) {
        return reportRepository.findByReporterUserIdOrderBySubmittedAtDesc(wargaId);
    }

    @Override  // ← POLYMORPHISM (Overload: 2 parameter)
    public List<Report> getReportsByDateRange(LocalDate start, LocalDate end) {
        LocalDateTime startDt = start.atStartOfDay();
        LocalDateTime endDt = end.atTime(LocalTime.MAX);
        return reportRepository.findByDateRange(startDt, endDt);
    }

    @Override  // ← POLYMORPHISM (Overload: 3 parameter beda)
    public List<Report> getReportsByStatusAndDateRange(Report.ReportStatus status,
                                                        LocalDate start,
                                                        LocalDate end) {
        LocalDateTime startDt = start.atStartOfDay();
        LocalDateTime endDt = end.atTime(LocalTime.MAX);
        return reportRepository.findByStatusAndDateRange(status, startDt, endDt);
    }

    @Override  // ← POLYMORPHISM
    public Report createReport(CreateReportDTO dto, String wargaId) {
        // ABSTRACTION: Controller tidak tahu detail pembuatan laporan
        User reporter = userRepository.findById(wargaId)
            .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

        Report report = new Report();
        report.setTicketNumber(generateTicketNumber());
        report.setReporter(reporter);
        report.setDescription(dto.getDescription());
        report.setLocationHint(dto.getLocationHint());
        report.setLatitude(dto.getLatitude());
        report.setLongitude(dto.getLongitude());
        report.setPhotoBase64(dto.getPhotoBase64());
        report.setSubmittedAt(LocalDateTime.now());
        report.setStatus(Report.ReportStatus.MENUNGGU_VALIDASI);

        if (dto.getCategoryId() != null && !dto.getCategoryId().isBlank()) {
            categoryRepository.findById(dto.getCategoryId())
                .ifPresent(report::setCategory);
        }

        return reportRepository.save(report);
    }

    @Override  // ← POLYMORPHISM
    public Report updateStatus(String reportId, Report.ReportStatus newStatus,
                                String notes, String changedBy) {
        Report report = reportRepository.findById(reportId)
            .orElseThrow(() -> new RuntimeException("Report tidak ditemukan"));

        Report.ReportStatus oldStatus = report.getStatus();

        if (notes != null) {
            report.setAdminNotes(notes);
        }
        report.setStatus(newStatus);

        Report saved = reportRepository.save(report);

        // Buat revision record (audit trail)
        createRevision(saved, oldStatus, newStatus, notes, changedBy);

        return saved;
    }

    @Override  // ← POLYMORPHISM (Overload: 4 parameter, beda signature)
    public Report updateStatus(String reportId, Report.ReportStatus newStatus,
                                String rejectionReason, String adminNotes,
                                String changedBy) {
        Report report = reportRepository.findById(reportId)
            .orElseThrow(() -> new RuntimeException("Report tidak ditemukan"));

        Report.ReportStatus oldStatus = report.getStatus();
        report.setStatus(newStatus);
        report.setRejectionReason(rejectionReason);
        report.setAdminNotes(adminNotes);

        Report saved = reportRepository.save(report);

        createRevision(saved, oldStatus, newStatus, rejectionReason, changedBy);

        return saved;
    }

    @Override  // ← POLYMORPHISM
    public Report saveReportPhoto(String reportId, String photoBase64) {
        Report report = reportRepository.findById(reportId)
            .orElseThrow(() -> new RuntimeException("Report tidak ditemukan"));
        report.setPhotoBase64(photoBase64);
        return reportRepository.save(report);
    }

    @Override  // ← POLYMORPHISM
    public long countByStatus(Report.ReportStatus status) {
        return reportRepository.countByStatus(status);
    }

    @Override  // ← POLYMORPHISM
    public String generateTicketNumber() {
        return "RPT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    // ===========================
    // PRIVATE HELPER — ENKAPSULASI
    // Disembunyikan dari luar (private)
    // ===========================
    private void createRevision(Report report, Report.ReportStatus oldStatus,
                                 Report.ReportStatus newStatus,
                                 String notes, String changedBy) {
        ReportRevision revision = new ReportRevision();
        revision.setReport(report);
        revision.setOldStatus(oldStatus != null ? oldStatus.name() : null);
        revision.setNewStatus(newStatus.name());
        revision.setNotes(notes);
        revision.setChangedBy(changedBy != null ? changedBy : "SYSTEM");
        revision.setChangedAt(LocalDateTime.now());
        revisionRepository.save(revision);
    }
}
