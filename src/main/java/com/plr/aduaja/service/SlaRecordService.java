package com.plr.aduaja.service;

import com.plr.aduaja.model.Report;
import com.plr.aduaja.model.Report.ReportStatus;
import com.plr.aduaja.model.SlaRecord;
import com.plr.aduaja.model.SlaRecord.SlaStatus;
import com.plr.aduaja.model.ReportCategory;
import com.plr.aduaja.repository.ReportCategoryRepository;
import com.plr.aduaja.repository.ReportRepository;
import com.plr.aduaja.repository.SlaRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SlaRecordService {

    @Autowired
    private SlaRecordRepository slaRecordRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private ReportCategoryRepository categoryRepository;

    public List<SlaRecord> getAllSlaRecords() {
        return slaRecordRepository.findAll();
    }

    public Optional<SlaRecord> getSlaRecordByReportId(String reportId) {
        return slaRecordRepository.findByReportReportId(reportId);
    }

    public List<SlaRecord> getRecordsByStatus(SlaStatus status) {
        return slaRecordRepository.findByCurrentStatus(status);
    }

    public SlaRecord createSlaRecord(Report report) {
        SlaRecord sla = new SlaRecord();
        sla.setReport(report);
        sla.setSlaStartAt(LocalDateTime.now());

        ReportCategory category = report.getCategory();
        if (category != null && category.getSlaDurationHours() != null) {
            sla.setSlaDeadlineAt(LocalDateTime.now().plusHours(category.getSlaDurationHours()));
        } else {
            sla.setSlaDeadlineAt(LocalDateTime.now().plusHours(48));
        }

        sla.setCurrentStatus(SlaStatus.BERJALAN);
        sla.setTotalPausedMinutes(0);

        return slaRecordRepository.save(sla);
    }

    public SlaRecord pauseSla(String slaId, int pausedMinutes) {
        SlaRecord sla = slaRecordRepository.findById(slaId)
                .orElseThrow(() -> new RuntimeException("SLA record not found"));
        sla.setCurrentStatus(SlaStatus.TERTUNDA);
        sla.setTotalPausedMinutes(sla.getTotalPausedMinutes() + pausedMinutes);
        sla.setSlaDeadlineAt(sla.getSlaDeadlineAt().plusMinutes(pausedMinutes));
        return slaRecordRepository.save(sla);
    }

    public SlaRecord resumeSla(String slaId) {
        SlaRecord sla = slaRecordRepository.findById(slaId)
                .orElseThrow(() -> new RuntimeException("SLA record not found"));
        sla.setCurrentStatus(SlaStatus.BERJALAN);
        return slaRecordRepository.save(sla);
    }

    public SlaRecord completeSla(String slaId) {
        SlaRecord sla = slaRecordRepository.findById(slaId)
                .orElseThrow(() -> new RuntimeException("SLA record not found"));
        sla.setCurrentStatus(SlaStatus.SELESAI);
        sla.setCompletedAt(LocalDateTime.now());
        return slaRecordRepository.save(sla);
    }
}
