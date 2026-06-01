package com.plr.aduaja.service;

import com.plr.aduaja.model.ConfirmationRequest;
import com.plr.aduaja.model.ConfirmationRequest.ResponseType;
import com.plr.aduaja.model.Report;
import com.plr.aduaja.model.User;
import com.plr.aduaja.repository.ConfirmationRequestRepository;
import com.plr.aduaja.repository.ReportRepository;
import com.plr.aduaja.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// ============================================================
// POLYMORPHISM — @Override (Run-time Polymorphism)
// ============================================================
@Service
public class ConfirmationServiceImpl implements ConfirmationService {

    @Autowired
    private ConfirmationRequestRepository confirmationRequestRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    // FR-RSL-18: Notifikasi ke warga saat Selesai Otomatis
    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ReportService reportService;

    @Override  // ← POLYMORPHISM: Override dari interface
    @Transactional
    public ConfirmationRequest createConfirmation(String reportId, String wargaId, int deadlineHours) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report tidak ditemukan: " + reportId));
        User warga = userRepository.findById(wargaId)
                .orElseThrow(() -> new RuntimeException("Warga tidak ditemukan: " + wargaId));

        ConfirmationRequest confirmation = new ConfirmationRequest();
        confirmation.setReport(report);
        confirmation.setWarga(warga);
        confirmation.setDeadlineAt(LocalDateTime.now().plusHours(deadlineHours));
        confirmation.setIsLocked(false);

        return confirmationRequestRepository.save(confirmation);
    }

    @Override  // ← POLYMORPHISM: Override dari interface
    @Transactional
    public ConfirmationRequest respond(String reportId, ResponseType response) {
        ConfirmationRequest confirmation = confirmationRequestRepository.findByReportReportId(reportId)
                .orElseThrow(() -> new RuntimeException("Confirmation tidak ditemukan untuk report: " + reportId));

        confirmation.setResponse(response);
        confirmation.setRespondedAt(LocalDateTime.now());
        confirmation.setIsLocked(true);

        // FR-RSL-06: TERIMA → SELESAI, TOLAK → SENGKETA
        Report report = confirmation.getReport();
        Report.ReportStatus oldStatus = report.getStatus();
        if (response == ResponseType.TERIMA) {
            report.setStatus(Report.ReportStatus.SELESAI);
            reportRepository.save(report);
            reportService.addReportRevision(report, oldStatus, Report.ReportStatus.SELESAI,
                "Warga menerima hasil tugas", "WARGA");
        } else if (response == ResponseType.TOLAK) {
            report.setStatus(Report.ReportStatus.SENGKETA);
            reportRepository.save(report);
            reportService.addReportRevision(report, oldStatus, Report.ReportStatus.SENGKETA,
                "Warga menolak hasil tugas", "WARGA");
        }

        return confirmationRequestRepository.save(confirmation);
    }

    @Override  // ← POLYMORPHISM: Override dari interface
    public Optional<ConfirmationRequest> getByReportId(String reportId) {
        return confirmationRequestRepository.findByReportReportId(reportId);
    }

    @Override  // ← POLYMORPHISM: Override dari interface (OVERLOAD — 1 param ResponseType)
    public List<ConfirmationRequest> getRequests(ResponseType responseType) {
        return confirmationRequestRepository.findByResponse(responseType);
    }

    @Override  // ← POLYMORPHISM: Override dari interface (OVERLOAD — 1 param String)
    public List<ConfirmationRequest> getRequests(String reportId) {
        return confirmationRequestRepository.findByReportReportId(reportId)
                .map(List::of)
                .orElse(List.of());
    }

    @Override  // ← POLYMORPHISM: Override dari interface
    public List<ConfirmationRequest> getAllRequests() {
        return confirmationRequestRepository.findAll();
    }

    @Override  // ← POLYMORPHISM: Override dari interface
    @Transactional
    public void processTimeouts() {
        List<ConfirmationRequest> timedOut = confirmationRequestRepository.findByDeadlineAtBeforeAndResponseIsNull(LocalDateTime.now());
        for (ConfirmationRequest confirmation : timedOut) {
            confirmation.setResponse(ResponseType.TIMEOUT);
            confirmation.setRespondedAt(LocalDateTime.now());
            confirmation.setIsLocked(true);
            // FR-RSL-05: Selesai Otomatis saat timeout tanpa respons warga
            Report report = confirmation.getReport();
            Report.ReportStatus oldStatus = report.getStatus();
            report.setStatus(Report.ReportStatus.SELESAI_OTOMATIS);
            reportRepository.save(report);
            reportService.addReportRevision(report, oldStatus, Report.ReportStatus.SELESAI_OTOMATIS,
                "Batas waktu konfirmasi 3x24 jam habis, laporan ditutup otomatis", "SYSTEM");

            // FR-RSL-18: Kirim notifikasi ke warga saat Selesai Otomatis
            try {
                String wargaId = report.getReporter() != null ? report.getReporter().getUserId() : null;
                if (wargaId != null) {
                    notificationService.createNotification(
                        wargaId,
                        "Laporan Ditutup Otomatis",
                        "Laporan " + report.getTicketNumber() + " telah ditutup secara otomatis karena batas waktu konfirmasi (3x24 jam kerja) telah habis tanpa respons dari Anda.",
                        "REPORT",
                        report.getReportId()
                    );
                }
            } catch (Exception ignored) {
                // Tidak boleh hentikan proses timeout jika notifikasi gagal
            }
        }
        confirmationRequestRepository.saveAll(timedOut);
    }
}
