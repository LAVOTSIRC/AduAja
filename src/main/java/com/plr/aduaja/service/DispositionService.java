package com.plr.aduaja.service;

import com.plr.aduaja.model.*;
import com.plr.aduaja.model.SlaRecord.SlaStatus;
import com.plr.aduaja.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DispositionService {

    @Autowired
    private DispositionRepository dispositionRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AgencyRepository agencyRepository;

    @Autowired
    private SlaRecordRepository slaRecordRepository;

    public List<Disposition> getAllDispositions() {
        return dispositionRepository.findAll();
    }

    public Optional<Disposition> getDispositionById(String id) {
        return dispositionRepository.findById(id);
    }

    public Optional<Disposition> getDispositionByReportId(String reportId) {
        return dispositionRepository.findByReportReportId(reportId);
    }

    public List<Disposition> getDispositionsByAgency(String agencyId) {
        return dispositionRepository.findByTargetAgencyAgencyIdOrderByDispatchedAtDesc(agencyId);
    }

    public Disposition createDisposition(String reportId, String dispatchedById, String targetAgencyId, String notes) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        User dispatchedBy = userRepository.findById(dispatchedById)
                .orElseThrow(() -> new RuntimeException("Dispatcher not found"));
        Agency targetAgency = agencyRepository.findById(targetAgencyId)
                .orElseThrow(() -> new RuntimeException("Target agency not found"));

        Disposition disposition = new Disposition();
        disposition.setReport(report);
        disposition.setDispatchedBy(dispatchedBy);
        disposition.setTargetAgency(targetAgency);
        disposition.setDispatchedAt(LocalDateTime.now());
        disposition.setNotes(notes);

        Disposition saved = dispositionRepository.save(disposition);

        SlaRecord sla = new SlaRecord();
        sla.setReport(report);
        sla.setSlaStartAt(LocalDateTime.now());
        if (report.getCategory() != null && report.getCategory().getSlaDurationHours() != null) {
            sla.setSlaDeadlineAt(LocalDateTime.now().plusHours(report.getCategory().getSlaDurationHours()));
        } else {
            sla.setSlaDeadlineAt(LocalDateTime.now().plusHours(48));
        }
        sla.setCurrentStatus(SlaStatus.BERJALAN);
        sla.setTotalPausedMinutes(0);
        slaRecordRepository.save(sla);

        return saved;
    }
}
