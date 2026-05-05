package com.plr.aduaja.service;

import com.plr.aduaja.model.*;
import com.plr.aduaja.model.ConfirmationRequest.ResponseType;
import com.plr.aduaja.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ConfirmationService {

    @Autowired
    private ConfirmationRequestRepository confirmationRequestRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    public Optional<ConfirmationRequest> getByReportId(String reportId) {
        return confirmationRequestRepository.findByReportReportId(reportId);
    }

    public ConfirmationRequest createConfirmation(String reportId, String wargaId, int deadlineHours) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        User warga = userRepository.findById(wargaId)
                .orElseThrow(() -> new RuntimeException("Warga not found"));

        ConfirmationRequest confirmation = new ConfirmationRequest();
        confirmation.setReport(report);
        confirmation.setWarga(warga);
        confirmation.setDeadlineAt(LocalDateTime.now().plusHours(deadlineHours));
        confirmation.setIsLocked(false);

        return confirmationRequestRepository.save(confirmation);
    }

    public ConfirmationRequest respond(String reportId, ResponseType response) {
        ConfirmationRequest confirmation = confirmationRequestRepository.findByReportReportId(reportId)
                .orElseThrow(() -> new RuntimeException("Confirmation not found"));

        confirmation.setResponse(response);
        confirmation.setRespondedAt(LocalDateTime.now());
        confirmation.setIsLocked(true);

        if (response == ResponseType.TERIMA) {
            confirmation.getReport().setStatus(Report.ReportStatus.DITUTUP);
            confirmation.getReport().setUpdatedAt(LocalDateTime.now());
            reportRepository.save(confirmation.getReport());
        } else if (response == ResponseType.TOLAK) {
            confirmation.getReport().setStatus(Report.ReportStatus.SENGKETA);
            confirmation.getReport().setUpdatedAt(LocalDateTime.now());
            reportRepository.save(confirmation.getReport());
        }

        return confirmationRequestRepository.save(confirmation);
    }

    public void processTimeouts() {
        List<ConfirmationRequest> timedOut = confirmationRequestRepository.findByDeadlineAtBeforeAndResponseIsNull(LocalDateTime.now());
        for (ConfirmationRequest confirmation : timedOut) {
            confirmation.setResponse(ResponseType.TIMEOUT);
            confirmation.setRespondedAt(LocalDateTime.now());
            confirmation.setIsLocked(true);
            confirmation.getReport().setStatus(Report.ReportStatus.DITUTUP);
            confirmation.getReport().setUpdatedAt(LocalDateTime.now());
            reportRepository.save(confirmation.getReport());
        }
        confirmationRequestRepository.saveAll(timedOut);
    }
}
