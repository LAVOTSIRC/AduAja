package com.plr.aduaja.controller;

import com.plr.aduaja.model.Report;
import com.plr.aduaja.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/reports")
public class ReportApiController {

    @Autowired
    private ReportService reportService;

    @GetMapping
    public ResponseEntity<List<Report>> getAllReports() {
        return ResponseEntity.ok(reportService.getAllReports());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Report> getReportById(@PathVariable String id) {
        Optional<Report> report = reportService.getReportById(id);
        return report.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Report>> getReportsByStatus(@PathVariable Report.Status status) {
        return ResponseEntity.ok(reportService.getReportsByStatus(status));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Report>> getReportsByUser(@PathVariable String userId) {
        return ResponseEntity.ok(reportService.getReportsByUser(userId));
    }

    @GetMapping("/disposisi-ready")
    public ResponseEntity<List<Report>> getReportsForDisposisi() {
        return ResponseEntity.ok(reportService.getReportsForDisposisi());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Report>> searchReports(@RequestParam String q) {
        return ResponseEntity.ok(reportService.searchReports(q));
    }

    @PostMapping
    public ResponseEntity<Report> createReport(@RequestBody Report report,
                                                @RequestParam String userId) {
        try {
            Report created = reportService.createReport(report, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/validate/{id}")
    public ResponseEntity<Report> validateReport(@PathVariable String id,
                                                  @RequestParam boolean approved,
                                                  @RequestParam(required = false) String rejectionReason,
                                                  @RequestParam String validatedBy) {
        try {
            Report report = reportService.validateReport(id, approved, rejectionReason, validatedBy);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Report> updateStatus(@PathVariable String id,
                                                @RequestParam Report.Status status) {
        try {
            Report report = reportService.updateStatus(id, status);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReport(@PathVariable String id) {
        try {
            reportService.updateStatus(id, Report.Status.DITOLAK);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getReportCounts() {
        Map<String, Long> counts = Map.of(
                "menunggu", reportService.countByStatus(Report.Status.MENUNGGU),
                "divalidasi", reportService.countByStatus(Report.Status.DIVALIDASI),
                "diproses", reportService.countByStatus(Report.Status.DIPROSES),
                "selesai", reportService.countByStatus(Report.Status.SELESAI),
                "ditolak", reportService.countByStatus(Report.Status.DITOLAK)
        );
        return ResponseEntity.ok(counts);
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<Report> confirmCompletion(@PathVariable String id,
                                                     @RequestParam String confirmedBy) {
        try {
            Report report = reportService.updateStatus(id, Report.Status.SELESAI);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/{id}/dispute")
    public ResponseEntity<Report> fileDispute(@PathVariable String id,
                                               @RequestParam String disputedBy) {
        try {
            Report report = reportService.updateStatus(id, Report.Status.SENGKETA);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/{id}/revisions")
    public ResponseEntity<Report> submitRevision(@PathVariable String id,
                                                  @RequestParam String description) {
        try {
            Report report = reportService.getReportById(id)
                    .orElseThrow(() -> new RuntimeException("Report not found"));
            report.setDescription(description);
            report.setStatus(Report.Status.DIVALIDASI);
            reportService.updateStatus(id, Report.Status.DIVALIDASI);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}
