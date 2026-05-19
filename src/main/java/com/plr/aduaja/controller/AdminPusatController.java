package com.plr.aduaja.controller;

import lombok.extern.slf4j.Slf4j;
import com.plr.aduaja.dto.DispositionDTO;
import com.plr.aduaja.dto.MergeDTO;
import com.plr.aduaja.model.*;
import com.plr.aduaja.model.Report.ReportStatus;
import com.plr.aduaja.repository.ReportCategoryRepository;
import com.plr.aduaja.repository.UserRepository;
import com.plr.aduaja.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Controller
public class AdminPusatController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private UserService userService;

    @Autowired
    private FieldTaskService fieldTaskService;

    @Autowired
    private DispositionService dispositionService;

    @Autowired
    private DisputeService disputeService;

    @Autowired
    private MergeRecordService mergeRecordService;

    @Autowired
    private AgencyService agencyService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReportCategoryRepository reportCategoryRepository;

    @Autowired
    private SlaRecordService slaRecordService;

    @Autowired
    private SlaMonitoringService slaMonitoringService;

    // ==========================================
    // ADMIN PUSAT — DASHBOARD
    // ==========================================

    @GetMapping("/admin/home")
    public String adminHome() {
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(
            Model model,
            @RequestParam(value = "role", required = false, defaultValue = "admin_pusat") String role,
            @RequestParam(value = "tab", required = false, defaultValue = "queue") String tab,
            @RequestParam(value = "id", required = false) String id
    ) {
        model.addAttribute("userRole", role);

        if ("admin_dinas".equalsIgnoreCase(role)) {
            model.addAttribute("dinasName", "Dinas Pekerjaan Umum");
            long diterima = reportService.countByStatus(Report.ReportStatus.DIDISPOSISI);
            long diproses = fieldTaskService.countByStatus(FieldTask.TaskStatus.SEDANG_DIKERJAKAN);
            long baru = fieldTaskService.countByStatus(FieldTask.TaskStatus.BARU);
            long selesai = fieldTaskService.countByStatus(FieldTask.TaskStatus.SELESAI);
            List<Map<String, Object>> dinasStats = new ArrayList<>();
            dinasStats.add(Map.of("title", "Laporan Diterima", "value", diterima,
                    "icon", "inbox", "bgColor", "bg-blue-100", "color", "text-blue-600"));
            dinasStats.add(Map.of("title", "Tugas Baru", "value", baru,
                    "icon", "inbox", "bgColor", "bg-indigo-100", "color", "text-indigo-600"));
            dinasStats.add(Map.of("title", "Dalam Penanganan", "value", diproses,
                    "icon", "wrench", "bgColor", "bg-yellow-100", "color", "text-yellow-600"));
            dinasStats.add(Map.of("title", "Selesai", "value", selesai,
                    "icon", "check-circle", "bgColor", "bg-green-100", "color", "text-green-600"));
            model.addAttribute("stats", dinasStats);
            List<Map<String, Object>> pendingAssignments = new ArrayList<>();
            List<Disposition> allDisp = dispositionService.getAllDispositions();
            for (Disposition d : allDisp) {
                if (d.getReport() != null) {
                    List<FieldTask> existingTasks = fieldTaskService.getTasksByReport(d.getReport().getReportId());
                    if (existingTasks.isEmpty()) {
                        Map<String, Object> m = new HashMap<>();
                        m.put("id", d.getReport().getReportId());
                        m.put("judul", d.getReport().getTicketNumber() != null ? d.getReport().getTicketNumber() : "Laporan");
                        m.put("kategori", d.getReport().getCategory() != null ? d.getReport().getCategory().getCategoryName() : "Lainnya");
                        m.put("prioritas", "Sedang");
                        m.put("slaStatus", "-");
                        pendingAssignments.add(m);
                    }
                }
            }
            model.addAttribute("pendingAssignments", pendingAssignments);
            List<User> realPetugas = userService.findByRole(User.Role.PETUGAS);
            List<Map<String, Object>> petugasList = realPetugas.stream().map(p -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id", p.getUserId());
                m.put("nama", p.getFullName());
                m.put("nip", "-");
                m.put("statusKetersediaan", "Tersedia");
                m.put("wilayahTugas", "-");
                m.put("tugasAktif", (int) fieldTaskService.getTasksByOfficerAndStatus(p.getUserId(), FieldTask.TaskStatus.SEDANG_DIKERJAKAN).size());
                m.put("kontak", p.getEmail());
                return m;
            }).collect(Collectors.toList());
            model.addAttribute("availablePetugas", petugasList.isEmpty() ? new ArrayList<>() : petugasList);
        } else {
            long laporanMasuk = reportService.countByStatus(Report.ReportStatus.MENUNGGU_VALIDASI);
            long menungguValidasi = reportService.countByStatus(Report.ReportStatus.MENUNGGU_VALIDASI);
            long dalamAntreanDinas = reportService.countByStatus(Report.ReportStatus.DIVALIDASI);
            long selesaiHariIni = reportService.countByStatus(Report.ReportStatus.SELESAI);
            List<Map<String, Object>> stats = new ArrayList<>();
            stats.add(Map.of("title", "Laporan Masuk", "value", laporanMasuk,
                    "icon", "file-text", "bgColor", "bg-blue-100", "color", "text-blue-600"));
            stats.add(Map.of("title", "Menunggu Validasi", "value", menungguValidasi,
                    "icon", "clock", "bgColor", "bg-yellow-100", "color", "text-yellow-600"));
            stats.add(Map.of("title", "Dalam Antrean Dinas", "value", dalamAntreanDinas,
                    "icon", "alert-triangle", "bgColor", "bg-red-100", "color", "text-red-600"));
            stats.add(Map.of("title", "Selesai Hari Ini", "value", selesaiHariIni,
                    "icon", "check-circle", "bgColor", "bg-green-100", "color", "text-green-600"));
            model.addAttribute("stats", stats);
        }

        List<Map<String, Object>> panels = new ArrayList<>();
        panels.add(Map.of("title", "Antrean Laporan", "description", "Daftar laporan masuk yang perlu divalidasi (FR-ADM-01)", "icon", "file-text", "color", "bg-blue-100 text-blue-600", "href", "/admin/laporan-queue"));
        panels.add(Map.of("title", "Validasi Laporan", "description", "Periksa dan putuskan kelayakan laporan (FR-ADM-05 s/d 10)", "icon", "check-circle-2", "color", "bg-green-100 text-green-600", "href", "/admin/validation"));
        panels.add(Map.of("title", "Merge Tiket Duplikat", "description", "Deteksi dan gabungkan laporan serupa (FR-ADM-11 s/d 18)", "icon", "git-merge", "color", "bg-yellow-100 text-yellow-600", "href", "/admin/merge"));
        panels.add(Map.of("title", "Disposisi ke Dinas", "description", "Kirim laporan ke dinas terkait (FR-DSP-01 s/d 06)", "icon", "send", "color", "bg-purple-100 text-purple-600", "href", "/admin/disposisi"));
        panels.add(Map.of("title", "Sengketa", "description", "Kelola banding dan resolusi sengketa (FR-RSL-09 s/d 13)", "icon", "scale", "color", "bg-orange-100 text-orange-600", "href", "/admin/sengketa"));
        model.addAttribute("panels", panels);

        List<Map<String, Object>> queueReports = getAdminValidationList();
        DateTimeFormatter queueFmt = DateTimeFormatter.ofPattern("dd MMM yyyy");
        queueReports.sort(Comparator.comparing(r -> {
            try { return LocalDate.parse((String) r.get("tanggalMasuk"), queueFmt); }
            catch (Exception e) { return LocalDate.MIN; }
        }));
        model.addAttribute("queueReports", queueReports);

        List<Map<String, Object>> validationReports = getAdminValidationList();
        model.addAttribute("validationReports", validationReports);

        Map<String, Object> selected = null;
        if (id != null) {
            selected = validationReports.stream()
                    .filter(r -> id.equals(String.valueOf(r.get("id"))))
                    .findFirst().orElse(null);
        }
        model.addAttribute("selectedReport", selected);

        List<MergeRecord> activeMerges = getActiveMerges();
        Set<String> mergedChildIds = getMergedChildIds(activeMerges);
        List<Report> mergeCandidates = reportService.getReportsByStatus(Report.ReportStatus.MENUNGGU_VALIDASI);
        List<Map<String, Object>> mergeTickets = mergeCandidates.stream()
            .filter(r -> !mergedChildIds.contains(r.getReportId()))
            .map(this::toMergeTicketMap)
            .collect(Collectors.toList());
        model.addAttribute("mergeTickets", mergeTickets);
        model.addAttribute("clusters", buildMergeClusters(activeMerges));
        model.addAttribute("selectedTickets", new ArrayList<>());
        model.addAttribute("hiddenChildCount", mergedChildIds.size());

        List<Map<String, Object>> disposisiReports = new ArrayList<>();
        List<Report> validated = reportService.getReportsByStatus(Report.ReportStatus.DIVALIDASI);
        for (Report r : validated) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", r.getReportId());
            m.put("judul", r.getTicketNumber() != null ? r.getTicketNumber() : "Laporan #" + r.getReportId().substring(0, 8));
            m.put("kategori", r.getCategory() != null ? r.getCategory().getCategoryName() : "Lainnya");
            m.put("pelapor", r.getReporter() != null ? r.getReporter().getFullName() : "-");
            m.put("wilayah", r.getLocationHint() != null ? r.getLocationHint() : "-");
            m.put("status", "Tervalidasi");
            m.put("prioritasSistem", "Sedang");
            m.put("dinasRekomendasi", "Dinas Terkait");
            m.put("foto", r.getPhotoBase64() != null ? r.getPhotoBase64() : dummyReportImage());
            disposisiReports.add(m);
        }
        model.addAttribute("disposisiReports", disposisiReports);

        List<Agency> realAgencies = agencyService.getActiveAgencies();
        List<Map<String, Object>> dinasList = realAgencies.stream().map(a -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", a.getAgencyId());
            m.put("name", a.getAgencyName());
            m.put("kategori", a.getContactEmail() != null ? List.of(a.getContactEmail()) : List.of("Lainnya"));
            return m;
        }).collect(Collectors.toList());
        model.addAttribute("dinasList", dinasList);

        Map<String, Object> selectedDisposition = null;
        if ("disposisi".equalsIgnoreCase(tab)) {
            if (id != null && !id.trim().isEmpty()) {
                String targetId = id.trim();
                for (Map<String, Object> r : disposisiReports) {
                    Object rid = r.get("id");
                    if (rid != null && targetId.equals(String.valueOf(rid))) {
                        selectedDisposition = r;
                        break;
                    }
                }
                if (selectedDisposition == null && !disposisiReports.isEmpty()) {
                    selectedDisposition = disposisiReports.get(0);
                }
            } else if (!disposisiReports.isEmpty()) {
                selectedDisposition = disposisiReports.get(0);
            }
        }
        model.addAttribute("selectedDisposition", selectedDisposition);

        return "admin/dashboard";
    }

    // ==========================================
    // ADMIN PUSAT — QUEUE DETAIL & DISPOSISI DETAIL
    // ==========================================

    @GetMapping("/admin/dashboard/detail")
    public String adminQueueDetail(
            Model model,
            @RequestParam(value = "id", required = false) String id
    ) {
        Map<String, Object> selectedReport = null;
        boolean isInDisposisi = false;

        if (id != null && !id.trim().isEmpty()) {
            List<Map<String, Object>> validationReports = getAdminValidationList();
            for (Map<String, Object> r : validationReports) {
                if (id.trim().equals(String.valueOf(r.get("id")))) {
                    selectedReport = r;
                    break;
                }
            }
            if (selectedReport == null) {
                Report r = reportService.findById(id.trim()).orElse(null);
                if (r != null && r.getStatus() == ReportStatus.DIVALIDASI) {
                    selectedReport = toAdminValidationMap(r);
                    selectedReport.put("status", "Tervalidasi");
                    isInDisposisi = true;
                }
            }
        }

        model.addAttribute("selectedReport", selectedReport);
        model.addAttribute("isInDisposisi", isInDisposisi);
        return "admin/queue-detail";
    }

    @GetMapping("/admin/dashboard/disposisi-detail")
    public String adminDisposisiDetail(
            Model model,
            @RequestParam(value = "id", required = false) String id
    ) {
        Map<String, Object> selectedDisposition = null;
        if (id != null && !id.trim().isEmpty()) {
            Report r = reportService.findById(id.trim()).orElse(null);
            if (r != null) {
                selectedDisposition = new HashMap<>();
                selectedDisposition.put("id", r.getReportId());
                selectedDisposition.put("judul", r.getTicketNumber() != null
                        ? r.getTicketNumber()
                        : "Laporan #" + r.getReportId().substring(0, 8));
                selectedDisposition.put("kategori", r.getCategory() != null
                        ? r.getCategory().getCategoryName() : "Lainnya");
                selectedDisposition.put("pelapor", r.getReporter() != null
                        ? r.getReporter().getFullName() : "-");
                selectedDisposition.put("wilayah", r.getLocationHint() != null
                        ? r.getLocationHint() : "-");
                selectedDisposition.put("deskripsi", r.getDescription() != null
                        ? r.getDescription() : "-");
                selectedDisposition.put("status", r.getStatus() != null
                        ? r.getStatus().name() : "TIDAK_DIKETAHUI");
                selectedDisposition.put("prioritasSistem", "Sedang");
                selectedDisposition.put("dinasRekomendasi", "");
                selectedDisposition.put("instruksiAdmin", "");
                selectedDisposition.put("deadline", "");
                selectedDisposition.put("foto", r.getPhotoBase64() != null
                        ? r.getPhotoBase64() : dummyReportImage());

                final Map<String, Object> dispMap = selectedDisposition;
                dispositionService.getDispositionByReportId(r.getReportId()).ifPresent(d -> {
                    if (d.getTargetAgency() != null) {
                        dispMap.put("dinasRekomendasi", d.getTargetAgency().getAgencyName());
                    }
                    if (d.getNotes() != null) {
                        dispMap.put("instruksiAdmin", d.getNotes());
                    }
                });
            }
        }

        List<Agency> realAgencies = agencyService.getActiveAgencies();
        List<Map<String, Object>> dinasList = realAgencies.stream().map(a -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", a.getAgencyId());
            m.put("name", a.getAgencyName());
            m.put("kategori", a.getContactEmail() != null ? List.of(a.getContactEmail()) : List.of("Lainnya"));
            return m;
        }).collect(Collectors.toList());
        model.addAttribute("dinasList", dinasList);
        model.addAttribute("selectedDisposition", selectedDisposition);
        return "admin/disposisi-detail";
    }

    @GetMapping("/admin/disposisi-detail")
    public String adminDisposisiDetailDirect(
            Model model,
            @RequestParam(value = "id", required = false) String id
    ) {
        return adminDisposisiDetail(model, id);
    }

    // ==========================================
    // ADMIN PUSAT — LAPORAN QUEUE
    // ==========================================

    @GetMapping("/admin/laporan-queue")
    public String adminLaporanQueue(Model model) {
        List<Map<String, Object>> reports = getAdminValidationList();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy");
        reports.sort(Comparator.comparing(r -> {
            try { return LocalDate.parse((String) r.get("tanggalMasuk"), fmt); }
            catch (Exception e) { return LocalDate.MIN; }
        }));
        model.addAttribute("queueReports", reports);
        return "admin/laporan-queue";
    }

    // ==========================================
    // ADMIN PUSAT — VALIDASI
    // ==========================================

    @GetMapping("/admin/validation")
    public String adminValidationPanel(
            Model model,
            @RequestParam(value = "id", required = false) String id
    ) {
        List<Map<String, Object>> reports = getAdminValidationList();
        model.addAttribute("reports", reports);
        model.addAttribute("pendingCount", reports.size());

        Map<String, Object> selected = null;
        if (id != null && !reports.isEmpty()) {
            selected = reports.stream()
                    .filter(r -> r.get("id").equals(id))
                    .findFirst().orElse(reports.get(0));
        }
        model.addAttribute("selectedReport", selected);
        return "admin/validation-panel";
    }

    @PostMapping("/admin/validation")
    public String adminValidationPost(
            @RequestParam(value = "id", required = false) String id,
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "reason", required = false) String reason,
            @RequestParam(value = "rejectionReason", required = false) String rejectionReason
    ) {
        String ticketId = id != null ? id.trim() : null;
        String normalizedAction = action != null ? action.trim().toLowerCase() : null;
        String note = (reason != null && !reason.trim().isEmpty())
                ? reason.trim()
                : (rejectionReason != null && !rejectionReason.trim().isEmpty() ? rejectionReason.trim() : null);

        if (ticketId == null || ticketId.isEmpty() || normalizedAction == null) {
            return "redirect:/admin/dashboard?tab=queue";
        }

        try {
            boolean approved = "approved".equals(normalizedAction) || "approve".equals(normalizedAction);
            if (ticketId != null) {
                Report r = reportService.updateStatus(ticketId, approved ? ReportStatus.DIVALIDASI : ReportStatus.DITOLAK);
                if (r != null && r.getReporter() != null) {
                    String notifTitle = approved ? "Laporan Divalidasi" : "Laporan Ditolak";
                    String notifMsg = approved
                            ? "Laporan Anda nomor " + r.getTicketNumber() + " telah divalidasi."
                            : "Laporan Anda nomor " + r.getTicketNumber() + " ditolak." + (note != null ? " Alasan: " + note : "");
                    notificationService.createNotification(
                            r.getReporter().getUserId(), notifTitle, notifMsg, "REPORT", r.getReportId()
                    );
                }
            }
        } catch (Exception e) {
            log.error("Gagal validasi laporan {}: {}", ticketId, e.getMessage(), e);
        }
        return "redirect:/admin/validation" + (ticketId != null ? "?id=" + ticketId : "");
    }

    @GetMapping("/admin/validation-panel")
    public String adminValidationPanelAlias(Model model,
                                            @RequestParam(value = "id", required = false) String id) {
        return "redirect:/admin/validation" + (id != null ? "?id=" + id : "");
    }

    @PostMapping("/admin/validation-panel")
    public String adminValidationPanelAliasPost(
            @RequestParam(value = "id", required = false) String id
    ) {
        return "redirect:/admin/validation" + (id != null ? "?id=" + id : "");
    }

    // ==========================================
    // ADMIN PUSAT — MERGE TIKET
    // ==========================================

    @GetMapping("/admin/merge")
    public String adminMergeTicketPanel(Model model) {
        List<MergeRecord> activeMerges = getActiveMerges();
        Set<String> mergedChildIds = getMergedChildIds(activeMerges);
        List<Report> mergeCandidates = reportService.getReportsByStatus(Report.ReportStatus.MENUNGGU_VALIDASI);
        List<Map<String, Object>> mergeTickets = mergeCandidates.stream()
                .filter(r -> !mergedChildIds.contains(r.getReportId()))
                .map(this::toMergeTicketMap)
                .collect(Collectors.toList());
        model.addAttribute("mergeTickets", mergeTickets);
        model.addAttribute("clusters", buildMergeClusters(activeMerges));
        model.addAttribute("selectedTickets", new ArrayList<>());
        model.addAttribute("hiddenChildCount", mergedChildIds.size());
        return "admin/merge-ticket-panel";
    }

    @GetMapping("/admin/merge-ticket-panel")
    public String adminMergeTicketPanelAlias(Model model) {
        return adminMergeTicketPanel(model);
    }

    @PostMapping("/admin/merge")
    public String adminMergeTicketPost(
            @RequestParam(value = "selectedTickets", required = false) List<String> selectedTickets,
            @RequestParam(value = "clusterIndex", required = false) Integer clusterIndex,
            @RequestParam(value = "primaryTicket", required = false) String primaryTicket,
            @RequestParam(value = "mergeReason", required = false) String mergeReason,
            HttpSession session
    ) {
        if (clusterIndex != null) {
            List<MergeRecord> activeMerges = getActiveMerges();
            List<List<MergeRecord>> clusters = buildMergeRecordClusters(activeMerges);
            if (clusterIndex >= 0 && clusterIndex < clusters.size()) {
                for (MergeRecord mr : clusters.get(clusterIndex)) {
                    mergeRecordService.cancelMerge(mr.getMergeId());
                }
                return "redirect:/admin/dashboard?tab=merge&separated=true";
            }
            return "redirect:/admin/dashboard?tab=merge";
        }

        if (selectedTickets != null && selectedTickets.size() >= 2 && mergeReason != null && !mergeReason.trim().isEmpty()) {
            if (mergeReason.trim().length() < 20) {
                return "redirect:/admin/dashboard?tab=merge&mergeError=shortReason";
            }

            List<String> blockedTickets = new ArrayList<>();
            for (String tid : selectedTickets) {
                Report r = reportService.findById(tid).orElse(null);
                if (r == null || isMergeBlocked(r.getStatus())) {
                    blockedTickets.add(tid);
                }
            }
            if (!blockedTickets.isEmpty()) {
                return "redirect:/admin/dashboard?tab=merge&mergeError=blocked&blocked=" + String.join(",", blockedTickets);
            }

            if (primaryTicket == null || primaryTicket.trim().isEmpty()) {
                return "redirect:/admin/dashboard?tab=merge&mergeError=noParent";
            }
            if (!selectedTickets.contains(primaryTicket)) {
                return "redirect:/admin/dashboard?tab=merge&mergeError=invalidParent";
            }

            String adminId = (String) session.getAttribute("userId");
            if (adminId == null) {
                adminId = userService.getUserByEmail("admin@aduaja.go.id")
                        .map(User::getUserId).orElse(null);
            }
            if (adminId == null) {
                return "redirect:/admin/dashboard?tab=merge&mergeError=unauthorized";
            }

            for (String tid : selectedTickets) {
                if (tid.equals(primaryTicket)) continue;
                MergeDTO dto = new MergeDTO();
                dto.setPrimaryReportId(primaryTicket);
                dto.setMergedReportId(tid);
                dto.setReason(mergeReason.trim());
                dto.setSimilarityScore(0);
                try {
                    mergeRecordService.createMerge(dto, adminId);
                } catch (Exception e) {
                    log.error("Gagal merge tiket {} ke {}: {}", tid, primaryTicket, e.getMessage(), e);
                }
            }

            return "redirect:/admin/dashboard?tab=merge&merged=true";
        }

        return "redirect:/admin/dashboard?tab=merge";
    }

    @PostMapping("/admin/merge-ticket-panel")
    public String adminMergeTicketPanelAliasPost() {
        return "redirect:/admin/dashboard?tab=merge";
    }

    // ==========================================
    // ADMIN PUSAT — DISPOSISI
    // ==========================================

    @GetMapping("/admin/disposisi")
    public String adminDisposisiPanel(
            Model model,
            @RequestParam(value = "id", required = false) String id
    ) {
        List<Map<String, Object>> reports = new ArrayList<>();
        List<Report> validated = reportService.getReportsByStatus(Report.ReportStatus.DIVALIDASI);
        if (!validated.isEmpty()) {
            for (Report r : validated) {
                Map<String, Object> m = new HashMap<>();
                m.put("id", r.getReportId());
                m.put("judul", r.getTicketNumber() != null ? r.getTicketNumber() : "Laporan #" + r.getReportId().substring(0, 8));
                m.put("kategori", r.getCategory() != null ? r.getCategory().getCategoryName() : "Lainnya");
                m.put("pelapor", r.getReporter() != null ? r.getReporter().getFullName() : "-");
                m.put("wilayah", r.getLocationHint() != null ? r.getLocationHint() : "-");
                m.put("status", "Tervalidasi");
                m.put("prioritasSistem", "Sedang");
                m.put("dinasRekomendasi", "Dinas Terkait");
                m.put("foto", r.getPhotoBase64() != null ? r.getPhotoBase64() : dummyReportImage());
                reports.add(m);
            }
        }

        model.addAttribute("reports", reports);
        model.addAttribute("pendingCount", reports.size());

        List<Map<String, Object>> dinasList = new ArrayList<>();
        try {
            List<Agency> realAgencies = agencyService.getActiveAgencies();
            if (realAgencies != null) {
                dinasList = realAgencies.stream().map(a -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", a.getAgencyId());
                    m.put("name", a.getAgencyName());
                    m.put("kategori", a.getContactEmail() != null ? List.of(a.getContactEmail()) : List.of("Lainnya"));
                    return m;
                }).collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("Gagal memuat daftar dinas: {}", e.getMessage(), e);
        }
        model.addAttribute("dinasList", dinasList);

        Map<String, Object> selected = null;
        if (id != null) {
            selected = reports.stream()
                    .filter(r -> r.get("id").equals(id))
                    .findFirst().orElse(reports.isEmpty() ? null : reports.get(0));
        }
        model.addAttribute("selectedReport", selected);
        return "admin/disposisi-panel";
    }

    @PostMapping("/admin/disposisi")
    public String adminDisposisiPost(
            @RequestParam(value = "id", required = false) String id,
            @RequestParam(value = "dinasId", required = false) String dinasId,
            @RequestParam(value = "catatan", required = false) String catatan,
            HttpSession session
    ) {
        String ticketId = id != null ? id.trim() : null;

        DispositionDTO dto = new DispositionDTO();
        dto.setReportId(ticketId);
        dto.setTargetAgencyId(dinasId);
        dto.setNotes(catatan);

        try {
            if (dto.getReportId() != null && !dto.getReportId().isEmpty()) {
                String adminId = (String) session.getAttribute("userId");
                if (adminId == null) {
                    adminId = userService.getUserByEmail("admin@aduaja.go.id")
                            .map(User::getUserId).orElse(null);
                }
                dto.setDispatchedById(adminId);

                dispositionService.createDisposition(
                        dto.getReportId(),
                        dto.getDispatchedById(),
                        dto.getTargetAgencyId(),
                        dto.getNotes()
                );
                reportService.updateStatus(dto.getReportId(), Report.ReportStatus.DIDISPOSISI);
            }
        } catch (Exception e) {
            log.error("Gagal disposisi laporan {}: {}", ticketId, e.getMessage(), e);
        }

        return "redirect:/admin/disposisi" + (ticketId != null ? "?id=" + ticketId : "");
    }

    @GetMapping("/admin/disposisi-panel")
    public String adminDisposisiPanelAlias(Model model,
                                           @RequestParam(value = "id", required = false) String id) {
        return adminDisposisiPanel(model, id);
    }

    @PostMapping("/admin/disposisi-panel")
    public String adminDisposisiPanelAliasPost(
            @RequestParam(value = "id", required = false) String id
    ) {
        return "redirect:/admin/disposisi" + (id != null ? "?id=" + id : "");
    }

    // ==========================================
    // ADMIN PUSAT — SENGKETA
    // ==========================================

    @GetMapping("/admin/sengketa")
    public String adminSengketaPanel(
            Model model,
            @RequestParam(value = "id", required = false) String id
    ) {
        List<DisputeRecord> realDisputes = disputeService.getPendingDisputes();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy");
        List<Map<String, Object>> disputes = realDisputes.stream().map(d -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", d.getDisputeId());
            m.put("ticketId", d.getReport() != null ? d.getReport().getReportId() : "-");
            m.put("judul", d.getReasonText() != null ? d.getReasonText() : "Sengketa #" + d.getDisputeId().substring(0, 8));
            m.put("statusSengketa", d.getResolution() == null ? "Menunggu Tinjauan" : "Selesai");
            m.put("prioritas", "Sedang");
            m.put("tanggalSengketa", d.getFiledAt() != null ? d.getFiledAt().format(fmt) : "-");
            m.put("pelapor", d.getReport() != null && d.getReport().getReporter() != null
                    ? d.getReport().getReporter().getFullName() : "-");
            m.put("tanggalLaporan", d.getReport() != null && d.getReport().getSubmittedAt() != null
                    ? d.getReport().getSubmittedAt().format(fmt) : "-");
            m.put("tanggalSelesai", d.getResolvedAt() != null ? d.getResolvedAt().format(fmt) : "-");
            m.put("statusSebelum", "Selesai");
            m.put("alasanSengketa", d.getReasonText() != null ? d.getReasonText() : "-");
            String evidence = d.getEvidencePhotoUrl();
            m.put("fotoBuktiSengketa", (evidence != null && !evidence.isBlank()) ? evidence : dummyReportImage());
            m.put("fotoBuktiPerbaikan", dummyReportImage());
            m.put("keteranganDinas", d.getResolutionNotes() != null ? d.getResolutionNotes() : "-");
            m.put("dinas", "Dinas Terkait");
            m.put("petugasId", "-");
            m.put("petugasNama", "-");
            return m;
        }).collect(Collectors.toList());
        model.addAttribute("disputes", disputes);

        Map<String, Object> selected = null;
        if (id != null) {
            selected = disputes.stream()
                    .filter(d -> d.get("id").equals(id))
                    .findFirst().orElse(null);
        }
        if (selected == null && !disputes.isEmpty()) {
            selected = disputes.get(0);
        }
        model.addAttribute("selectedDispute", selected);
        return "admin/sengketa-panel";
    }

    @PostMapping("/admin/sengketa")
    public String adminSengketaPost(
            @RequestParam(value = "id", required = false) String id,
            @RequestParam(value = "keputusan", required = false) String keputusan,
            @RequestParam(value = "catatan", required = false) String catatan
    ) {
        try {
            String adminId = userService.getUserByEmail("admin@aduaja.go.id")
                    .map(User::getUserId).orElse(null);
            DisputeRecord.ResolutionType resolution = "tugaskan_kembali".equalsIgnoreCase(keputusan)
                    ? DisputeRecord.ResolutionType.TUGASKAN_KEMBALI
                    : DisputeRecord.ResolutionType.TUTUP_LAPORAN;
            disputeService.resolveDispute(id, resolution, adminId, catatan);
        } catch (Exception e) {
            log.error("Gagal resolusi sengketa {}: {}", id, e.getMessage(), e);
        }
        return "redirect:/admin/sengketa" + (id != null ? "?id=" + id : "");
    }

    @GetMapping("/admin/sengketa-panel")
    public String adminSengketaPanelAlias(Model model,
                                          @RequestParam(value = "id", required = false) String id) {
        return adminSengketaPanel(model, id);
    }

    @PostMapping("/admin/sengketa-panel")
    public String adminSengketaPanelAliasPost(
            @RequestParam(value = "id", required = false) String id
    ) {
        return "redirect:/admin/sengketa" + (id != null ? "?id=" + id : "");
    }

    // ==========================================
    // ADMIN PUSAT — SLA MONITORING
    // ==========================================

    @GetMapping("/admin/sla")
    public String slaPanel(Model model) {
        model.addAttribute("slaStats", slaMonitoringService.getSlaStatistics());
        model.addAttribute("lateItems", slaMonitoringService.getLateItems());
        model.addAttribute("allSla", slaRecordService.getAllRecords());
        return "admin/dashboard";
    }

    // ==========================================
    // PRIVATE HELPERS
    // ==========================================

    private String toStatusLabel(Report.ReportStatus status) {
        if (status == null) return "Menunggu";
        return switch (status) {
            case MENUNGGU_VALIDASI -> "Diterima";
            case PERLU_REVISI -> "Revisi";
            case DITOLAK -> "Ditolak";
            case DIVALIDASI -> "Tervalidasi";
            case DIDISPOSISI -> "Didisposisi";
            case DITUGASKAN -> "Ditugaskan";
            case SEDANG_DIKERJAKAN -> "Dalam Penanganan";
            case TERTUNDA -> "Tertunda";
            case MENUNGGU_KONFIRMASI -> "Menunggu Konfirmasi";
            case SELESAI -> "Selesai";
            case SENGKETA -> "Sengketa";
            case DITUTUP -> "Ditutup";
        };
    }

    private String toDateStr(LocalDateTime dt) {
        if (dt == null) return "-";
        return dt.format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
    }

    private Map<String, Object> toAdminValidationMap(Report r) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", r.getReportId());
        m.put("judul", r.getTicketNumber());
        m.put("kategori", r.getCategory() != null ? r.getCategory().getCategoryName() : "Lainnya");
        m.put("pelapor", r.getReporter() != null ? r.getReporter().getFullName() : "-");
        m.put("kontakPelapor", r.getReporter() != null ? r.getReporter().getEmail() : "-");
        m.put("wilayah", r.getLocationHint() != null ? r.getLocationHint() : "-");
        m.put("tanggalMasuk", toDateStr(r.getSubmittedAt()));
        m.put("status", toStatusLabel(r.getStatus()));
        m.put("prioritas", "Sedang");
        m.put("sisaWaktuSLA", "-");
        m.put("foto", r.getPhotoBase64() != null ? r.getPhotoBase64() : dummyReportImage());
        String lat = r.getLatitude() != null ? r.getLatitude().toPlainString() : "0";
        String lng = r.getLongitude() != null ? r.getLongitude().toPlainString() : "0";
        m.put("koordinatStr", lat + "," + lng);
        m.put("patokan", r.getLocationHint());
        m.put("waktuKejadian", r.getSubmittedAt() != null ? r.getSubmittedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")) : "-");
        m.put("deskripsi", r.getDescription() != null ? r.getDescription() : "-");
        return m;
    }

    private List<Map<String, Object>> getAdminValidationList() {
        List<Report> real = reportService.getReportsByStatus(Report.ReportStatus.MENUNGGU_VALIDASI);
        real.sort(Comparator.nullsLast(Comparator.comparing(Report::getSubmittedAt, Comparator.nullsLast(Comparator.naturalOrder()))));
        return real.stream().map(this::toAdminValidationMap).collect(java.util.stream.Collectors.toList());
    }

    private List<MergeRecord> getActiveMerges() {
        return mergeRecordService.getMerges().stream()
                .filter(m -> Boolean.TRUE.equals(m.getIsActive()))
                .collect(Collectors.toList());
    }

    private Set<String> getMergedChildIds(List<MergeRecord> activeMerges) {
        Set<String> ids = new HashSet<>();
        for (MergeRecord m : activeMerges) {
            if (m.getChildReport() != null && m.getChildReport().getReportId() != null) {
                ids.add(m.getChildReport().getReportId());
            }
        }
        return ids;
    }

    private List<List<MergeRecord>> buildMergeRecordClusters(List<MergeRecord> activeMerges) {
        Map<String, List<MergeRecord>> grouped = new HashMap<>();
        for (MergeRecord m : activeMerges) {
            if (m.getParentReport() == null || m.getParentReport().getReportId() == null) continue;
            String parentId = m.getParentReport().getReportId();
            grouped.computeIfAbsent(parentId, k -> new ArrayList<>()).add(m);
        }
        List<String> parentIds = new ArrayList<>(grouped.keySet());
        parentIds.sort(Comparator.naturalOrder());
        List<List<MergeRecord>> clusters = new ArrayList<>();
        for (String parentId : parentIds) {
            clusters.add(grouped.get(parentId));
        }
        return clusters;
    }

    private List<List<Map<String, Object>>> buildMergeClusters(List<MergeRecord> activeMerges) {
        List<List<MergeRecord>> recordClusters = buildMergeRecordClusters(activeMerges);
        List<List<Map<String, Object>>> clusters = new ArrayList<>();
        for (List<MergeRecord> clusterRecords : recordClusters) {
            if (clusterRecords.isEmpty()) continue;
            List<Map<String, Object>> cluster = new ArrayList<>();
            Report parent = clusterRecords.get(0).getParentReport();
            if (parent != null) {
                cluster.add(toMergeTicketMap(parent));
            }
            for (MergeRecord mr : clusterRecords) {
                Report child = mr.getChildReport();
                if (child != null) {
                    cluster.add(toMergeTicketMap(child));
                }
            }
            clusters.add(cluster);
        }
        return clusters;
    }

    private Map<String, Object> toMergeTicketMap(Report r) {
        Map<String, Object> m = toAdminValidationMap(r);
        String ticketStatus = toMergeTicketStatus(r.getStatus());
        m.put("ticketStatus", ticketStatus);
        m.put("isMergeable", !isMergeBlocked(r.getStatus()));
        m.putIfAbsent("similarityScore", 0);
        m.putIfAbsent("deskripsi", m.getOrDefault("judul", "-"));
        if (r.getPhotoBase64() == null || r.getPhotoBase64().isBlank()) {
            m.put("foto", dummyReportImage());
        }
        return m;
    }

    private String toMergeTicketStatus(ReportStatus status) {
        if (status == null) return "menunggu";
        return switch (status) {
            case DIDISPOSISI -> "disposisi";
            case DITUGASKAN, SEDANG_DIKERJAKAN -> "in-progress";
            default -> "menunggu";
        };
    }

    private boolean isMergeBlocked(ReportStatus status) {
        if (status == null) return false;
        return status == ReportStatus.DIDISPOSISI
                || status == ReportStatus.DITUGASKAN
                || status == ReportStatus.SEDANG_DIKERJAKAN
                || status == ReportStatus.SELESAI
                || status == ReportStatus.DITUTUP;
    }

    private String dummyReportImage() {
        return "data:image/svg+xml;charset=UTF-8,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1200' height='800' viewBox='0 0 1200 800'%3E%3Cdefs%3E%3ClinearGradient id='g' x1='0' x2='1' y1='0' y2='1'%3E%3Cstop offset='0%25' stop-color='%231d4ed8'/%3E%3Cstop offset='100%25' stop-color='%230f766e'/%3E%3C/linearGradient%3E%3C/defs%3E%3Crect width='1200' height='800' fill='url(%23g)'/%3E%3Crect x='70' y='70' width='1060' height='660' rx='36' fill='white' fill-opacity='0.12'/%3E%3Ctext x='600' y='390' text-anchor='middle' fill='white' font-family='Arial, sans-serif' font-size='64' font-weight='700'%3EAduAja%3C/text%3E%3Ctext x='600' y='460' text-anchor='middle' fill='white' font-family='Arial, sans-serif' font-size='28' fill-opacity='0.9'%3EDummy Report Image%3C/text%3E%3C/svg%3E";
    }
}
