package com.plr.aduaja.service;

import com.plr.aduaja.model.Report;
import com.plr.aduaja.model.SlaRecord;
import com.plr.aduaja.model.SlaRecord.SlaStatus;
import com.plr.aduaja.repository.SlaRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

// ============================================================
// POLYMORPHISM — @Override (Run-time Polymorphism)
// ENCAPSULATION — inject lewat Interface, field private
// ============================================================
@Service
public class SlaMonitoringServiceImpl implements SlaMonitoringService {

    @Autowired
    private SlaRecordRepository slaRecordRepository;

    // Scheduled job — cek SLA violations tiap jam
    @Scheduled(fixedRate = 3600000)
    public void checkSlaViolations() {
        LocalDateTime now = LocalDateTime.now();
        List<SlaRecord> overdue = slaRecordRepository.findOverdue(now);

        for (SlaRecord sla : overdue) {
            if (sla.getCurrentStatus() == SlaStatus.BERJALAN) {
                sla.setCurrentStatus(SlaStatus.TERLAMBAT);
                slaRecordRepository.save(sla);
            }
        }
    }

    @Override  // ← POLYMORPHISM: Override dari interface
    public Map<String, Object> getSlaStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", slaRecordRepository.count());
        stats.put("berjalan", slaRecordRepository.countByCurrentStatus(SlaStatus.BERJALAN));
        stats.put("tertunda", slaRecordRepository.countByCurrentStatus(SlaStatus.TERTUNDA));
        stats.put("terlambat", slaRecordRepository.countByCurrentStatus(SlaStatus.TERLAMBAT));
        stats.put("selesai", slaRecordRepository.countByCurrentStatus(SlaStatus.SELESAI));
        return stats;
    }

    @Override  // ← POLYMORPHISM: Override dari interface
    public List<Map<String, Object>> getLateItems() {
        LocalDateTime now = LocalDateTime.now();
        List<SlaRecord> overdue = slaRecordRepository.findOverdue(now);
        List<Map<String, Object>> items = new ArrayList<>();
        for (SlaRecord sla : overdue) {
            Map<String, Object> item = new HashMap<>();
            Report report = sla.getReport();
            if (report != null) {
                item.put("reportId", report.getReportId());
                item.put("ticketNumber", report.getTicketNumber());
                item.put("status", report.getStatus() != null ? report.getStatus().toString() : "N/A");
            }
            item.put("slaId", sla.getSlaId());
            item.put("deadline", sla.getSlaDeadlineAt());
            item.put("minutesLate", Duration.between(sla.getSlaDeadlineAt(), now).toMinutes());
            items.add(item);
        }
        return items;
    }

    @Override  // ← POLYMORPHISM: Override dari interface
    public Map<String, Object> getReportSlaStatus(String reportId) {
        Optional<SlaRecord> slaOpt = slaRecordRepository.findByReportReportId(reportId);
        Map<String, Object> result = new HashMap<>();
        if (slaOpt.isPresent()) {
            SlaRecord sla = slaOpt.get();
            result.put("slaId", sla.getSlaId());
            result.put("status", sla.getCurrentStatus());
            result.put("deadline", sla.getSlaDeadlineAt());
            result.put("pausedMinutes", sla.getTotalPausedMinutes());
        } else {
            result.put("status", "TIDAK_ADA");
        }
        return result;
    }

    @Override  // ← POLYMORPHISM: Override dari interface (OVERLOAD — tanpa parameter)
    public List<Map<String, Object>> getSlaSummary() {
        List<Map<String, Object>> summary = new ArrayList<>();
        Map<String, Object> stats = getSlaStatistics();
        summary.add(stats);
        return summary;
    }

    @Override  // ← POLYMORPHISM: Override dari interface (OVERLOAD — 1 parameter)
    public List<Map<String, Object>> getSlaSummary(String dinasId) {
        // Filter by dinasId bila diperlukan — saat ini return semua
        return getSlaSummary();
    }
}
