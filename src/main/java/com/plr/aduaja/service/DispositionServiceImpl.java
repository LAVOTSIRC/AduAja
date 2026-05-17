package com.plr.aduaja.service;

import com.plr.aduaja.model.*;
import com.plr.aduaja.model.SlaRecord.SlaStatus;
import com.plr.aduaja.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DispositionServiceImpl implements DispositionService {

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

    @Override
    public List<Disposition> getAllDispositions() {
        return dispositionRepository.findAll();
    }

    @Override
    public Optional<Disposition> getDispositionById(String id) {
        return dispositionRepository.findById(id);
    }

    @Override
    public Optional<Disposition> getDispositionByReportId(String reportId) {
        return dispositionRepository.findByReportReportId(reportId);
    }

    // =========================================================
    // OVERLOADING: getDispositions() — Compile-time Polymorphism
    // Nama method sama, parameter berbeda
    // =========================================================

    @Override
    public List<Disposition> getDispositions() {  // OVERLOAD 1: semua
        return dispositionRepository.findAll();
    }

    @Override
    public List<Disposition> getDispositions(String agencyId) {  // OVERLOAD 2: filter by agency
        return dispositionRepository.findByTargetAgencyAgencyIdOrderByDispatchedAtDesc(agencyId);
    }

    @Override
    public List<Disposition> getDispositions(LocalDateTime from, LocalDateTime to) {  // OVERLOAD 3: filter by date range
        return dispositionRepository.findAll().stream()
                .filter(d -> d.getDispatchedAt() != null
                        && !d.getDispatchedAt().isBefore(from)
                        && !d.getDispatchedAt().isAfter(to))
                .collect(Collectors.toList());
    }

    @Override
    public List<Disposition> getDispositions(String agencyId, LocalDateTime from, LocalDateTime to) {  // OVERLOAD 4
        return dispositionRepository.findByTargetAgencyAgencyIdOrderByDispatchedAtDesc(agencyId).stream()
                .filter(d -> d.getDispatchedAt() != null
                        && !d.getDispatchedAt().isBefore(from)
                        && !d.getDispatchedAt().isAfter(to))
                .collect(Collectors.toList());
    }

    @Override
    public List<Disposition> getDispositionsByAgency(String agencyId) {
        return dispositionRepository.findByTargetAgencyAgencyIdOrderByDispatchedAtDesc(agencyId);
    }

    @Override
    public List<Disposition> getDispositionsByDispatcher(String dispatchedByUserId) {
        return dispositionRepository.findByDispatchedByUserId(dispatchedByUserId);
    }

    @Override
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

        // FIX: hanya buat SlaRecord BARU jika belum ada untuk report ini
        // Mencegah duplicate row yang menyebabkan Hibernate ASSERT error pada @OneToOne
        boolean slaExists = slaRecordRepository.findByReportReportId(reportId).isPresent();
        if (!slaExists) {
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
        }

        return saved;
    }
}
