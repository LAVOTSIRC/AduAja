package com.plr.aduaja.service;

import com.plr.aduaja.dto.DisputeDTO;
import com.plr.aduaja.model.*;
import com.plr.aduaja.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MergeDisputeAndCascadeTest {

    @Autowired private DisputeService disputeService;
    @Autowired private ReportService reportService;
    @Autowired private UserRepository userRepository;
    @Autowired private ReportRepository reportRepository;
    @Autowired private DisputeRecordRepository disputeRecordRepository;
    @Autowired private MergeRecordRepository mergeRecordRepository;
    @Autowired private NotificationRepository notificationRepository;

    private User admin;
    private User petugas;
    private User childReporter;
    private Report parentReport;
    private Report childReport;

    @BeforeEach
    void setUp() {
        admin = new User();
        admin.setEmail("admin.merge.test@aduaja.go.id");
        admin.setFullName("Admin Merge Test");
        admin.setRole(User.Role.ADMIN_DINAS);
        admin.setPasswordHash("pass");
        admin.setAccountStatus(User.AccountStatus.ACTIVE);
        userRepository.save(admin);

        petugas = new User();
        petugas.setEmail("petugas.merge.test@aduaja.go.id");
        petugas.setFullName("Petugas Merge Test");
        petugas.setRole(User.Role.PETUGAS);
        petugas.setPasswordHash("pass");
        petugas.setAccountStatus(User.AccountStatus.ACTIVE);
        userRepository.save(petugas);

        childReporter = new User();
        childReporter.setEmail("warga.merge.test@aduaja.go.id");
        childReporter.setFullName("Warga Merge Test");
        childReporter.setRole(User.Role.WARGA);
        childReporter.setPasswordHash("pass");
        childReporter.setAccountStatus(User.AccountStatus.ACTIVE);
        userRepository.save(childReporter);

        parentReport = new Report();
        parentReport.setTicketNumber("PRN-" + System.currentTimeMillis() % 100000);
        parentReport.setDescription("Parent Report Merge");
        parentReport.setStatus(Report.ReportStatus.MENUNGGU_VALIDASI);
        parentReport.setSubmittedAt(LocalDateTime.now());
        parentReport.setReporter(petugas);
        reportRepository.save(parentReport);

        childReport = new Report();
        childReport.setTicketNumber("CHD-" + System.currentTimeMillis() % 100000);
        childReport.setDescription("Child Report Merge");
        childReport.setStatus(Report.ReportStatus.MENUNGGU_VALIDASI);
        childReport.setSubmittedAt(LocalDateTime.now());
        childReport.setReporter(childReporter);
        childReport.setParentReport(parentReport);
        reportRepository.save(childReport);

        MergeRecord mergeRecord = new MergeRecord();
        mergeRecord.setParentReport(parentReport);
        mergeRecord.setChildReport(childReport);
        mergeRecord.setMergedBy(admin);
        mergeRecord.setMergeReason("Test merge");
        mergeRecord.setIsActive(true);
        mergeRecord.setMergedAt(LocalDateTime.now());
        mergeRecordRepository.save(mergeRecord);
    }

    @Test
    void testDisputeOnChildFailsWhenParentAlreadyDisputed() {
        DisputeDTO dtoParent = new DisputeDTO();
        dtoParent.setReportId(parentReport.getReportId());
        dtoParent.setReason("Sengketa parent");
        dtoParent.setEvidencePhotoUrl("http://example.com/parent.jpg");
        disputeService.createDispute(dtoParent, petugas.getUserId());

        DisputeDTO dtoChild = new DisputeDTO();
        dtoChild.setReportId(childReport.getReportId());
        dtoChild.setReason("Sengketa child");
        dtoChild.setEvidencePhotoUrl("http://example.com/child.jpg");

        IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> disputeService.createDispute(dtoChild, petugas.getUserId()));
        assertTrue(ex.getMessage() != null && ex.getMessage().contains("Sengketa"));
    }

    @Test
    void testChildReporterReceivesNotificationOnStatusCascade() {
        reportService.cascadeStatusToChildren(parentReport.getReportId(),
            Report.ReportStatus.SELESAI, "Test cascade", admin.getUserId());

        List<Notification> notifications = notificationRepository
            .findByRecipientUserId(childReporter.getUserId());

        boolean foundCascade = notifications.stream()
            .anyMatch(n -> "Perubahan Status Laporan".equals(n.getTitle())
                && n.getMessageText() != null
                && n.getMessageText().contains(childReport.getTicketNumber()));
        assertTrue(foundCascade, "Notifikasi perubahan status child harus ditemukan");
    }

    @Test
    void testChildReportGetsNewStatusAfterCascade() {
        reportService.cascadeStatusToChildren(parentReport.getReportId(),
            Report.ReportStatus.DITUGASKAN, "Cascade test", admin.getUserId());

        Report refreshed = reportRepository.findById(childReport.getReportId()).orElseThrow();
        assertEquals(Report.ReportStatus.DITUGASKAN, refreshed.getStatus(),
            "Child harus mengikuti status parent setelah cascade");
    }

    @Test
    void testDirectDuplicateDisputeOnSameReportStillBlocked() {
        DisputeDTO dto = new DisputeDTO();
        dto.setReportId(parentReport.getReportId());
        dto.setReason("Sengketa pertama");
        dto.setEvidencePhotoUrl("http://example.com/first.jpg");
        disputeService.createDispute(dto, petugas.getUserId());

        DisputeDTO dto2 = new DisputeDTO();
        dto2.setReportId(parentReport.getReportId());
        dto2.setReason("Sengketa kedua");
        dto2.setEvidencePhotoUrl("http://example.com/second.jpg");

        assertThrows(IllegalStateException.class,
            () -> disputeService.createDispute(dto2, petugas.getUserId()));
    }
}
