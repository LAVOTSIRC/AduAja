package com.plr.aduaja.service;

import com.plr.aduaja.model.Report;
import com.plr.aduaja.model.FieldTask;
import com.plr.aduaja.model.SlaRecord;
import com.plr.aduaja.model.SlaRecord.SlaStatus;
import com.plr.aduaja.repository.ReportRepository;
import com.plr.aduaja.repository.FieldTaskRepository;
import com.plr.aduaja.repository.SlaRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SlaMonitoringService {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private FieldTaskRepository fieldTaskRepository;

    @Autowired
    private SlaRecordRepository slaRecordRepository;

    @Scheduled(fixedRate = 3600000)
    public void checkSlaViolations() {
        LocalDateTime now = LocalDateTime.now();
        List<SlaRecord> slaRecords = slaRecordRepository.findBySlaDeadlineAtBeforeAndCurrentStatusNot(
                now, SlaStatus.SELESAI);

        for (SlaRecord sla : slaRecords) {
            if (sla.getCurrentStatus() == SlaStatus.BERJALAN) {
                sla.setCurrentStatus(SlaStatus.TERLAMBAT);
                slaRecordRepository.save(sla);
                System.out.println("SLA Violation - Report: " + sla.getReport().getReportId() +
                        " - Deadline: " + sla.getSlaDeadlineAt());
            }
        }
    }

    public Map<String, Object> getSlaStatistics() {
        LocalDateTime now = LocalDateTime.now();
        Map<String, Object> stats = new HashMap<>();

        long totalSla = slaRecordRepository.count();
        long onTime = slaRecordRepository.countByCurrentStatus(SlaStatus.SELESAI);
        long late = slaRecordRepository.countByCurrentStatus(SlaStatus.TERLAMBAT);
        long running = slaRecordRepository.countByCurrentStatus(SlaStatus.BERJALAN);
        long paused = slaRecordRepository.countByCurrentStatus(SlaStatus.TERTUNDA);

        stats.put("totalSla", totalSla);
        stats.put("onTime", onTime);
        stats.put("late", late);
        stats.put("running", running);
        stats.put("paused", paused);

        return stats;
    }

    public List<Map<String, Object>> getLateItems() {
        LocalDateTime now = LocalDateTime.now();
        List<Map<String, Object>> lateItems = new ArrayList<>();

        List<SlaRecord> lateSla = slaRecordRepository.findBySlaDeadlineAtBeforeAndCurrentStatusNot(now, SlaStatus.SELESAI);
        for (SlaRecord sla : lateSla) {
            Report report = sla.getReport();
            Map<String, Object> item = new HashMap<>();
            item.put("reportId", report.getReportId());
            item.put("ticketNumber", report.getTicketNumber());
            item.put("status", report.getStatus().toString());
            item.put("slaDeadline", sla.getSlaDeadlineAt());
            long hoursLate = java.time.Duration.between(sla.getSlaDeadlineAt(), now).toHours();
            item.put("hoursLate", hoursLate);
            lateItems.add(item);
        }

        return lateItems;
    }
}
