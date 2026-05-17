package com.plr.aduaja.controller;

import com.plr.aduaja.model.*;
import com.plr.aduaja.model.Report.ReportStatus;
import com.plr.aduaja.model.FieldTask.TaskStatus;
import com.plr.aduaja.repository.ReportCategoryRepository;
import com.plr.aduaja.repository.UserProfileRepository;
import com.plr.aduaja.repository.UserRepository;
import com.plr.aduaja.dto.DispositionDTO;
import com.plr.aduaja.dto.CreateReportDTO;
import com.plr.aduaja.dto.LoginDTO;
import com.plr.aduaja.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class WebController {

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
    private AttendanceService attendanceService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private MergeRecordService mergeRecordService;

    @Autowired
    private AgencyService agencyService;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReportCategoryRepository reportCategoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ==========================================
    // ROOT REDIRECT
    // ==========================================
    @GetMapping("/")
    public String rootRedirect() {
        return "index";
    }

    // ==========================================
    // WARGA ROUTES - REGISTRATION & LOGIN
    // ==========================================

    // POST /warga/register — Ditangani oleh WargaAuthController

    // POST /warga/login — Ditangani oleh WargaAuthController

    // ==========================================
    // INDEX & LAYOUTS
    // ==========================================
    @GetMapping("/index")
    public String index() { return "index"; }

    @GetMapping("/layouts/master")
    public String layoutsMaster() { return "layouts/master"; }

    // ==========================================
    // ADMIN ROUTES — ADMIN PUSAT
    // ==========================================

    // GET /admin/login — Ditangani oleh AdminAuthController


    // POST /admin/login — Ditangani oleh AdminAuthController

    // POST /admin/logout — Ditangani oleh AdminAuthController

    /** GET /admin/home — redirect ke dashboard */
    @GetMapping("/admin/home")
    public String adminHome() {
        return "redirect:/admin/dashboard";
    }

    /** GET /admin/dashboard — dashboard utama admin (pusat atau dinas) */
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

        // Module panels
        List<Map<String, Object>> panels = new ArrayList<>();
        panels.add(Map.of(
                "title", "Antrean Laporan",
                "description", "Daftar laporan masuk yang perlu divalidasi (FR-ADM-01)",
                "icon", "file-text",
                "color", "bg-blue-100 text-blue-600",
                "href", "/admin/laporan-queue"
        ));
        panels.add(Map.of(
                "title", "Validasi Laporan",
                "description", "Periksa dan putuskan kelayakan laporan (FR-ADM-05 s/d 10)",
                "icon", "check-circle-2",
                "color", "bg-green-100 text-green-600",
                "href", "/admin/validation"
        ));
        panels.add(Map.of(
                "title", "Merge Tiket Duplikat",
                "description", "Deteksi dan gabungkan laporan serupa (FR-ADM-11 s/d 18)",
                "icon", "git-merge",
                "color", "bg-yellow-100 text-yellow-600",
                "href", "/admin/merge"
        ));
        panels.add(Map.of(
                "title", "Disposisi ke Dinas",
                "description", "Kirim laporan ke dinas terkait (FR-DSP-01 s/d 06)",
                "icon", "send",
                "color", "bg-purple-100 text-purple-600",
                "href", "/admin/disposisi"
        ));
        panels.add(Map.of(
            "title", "Sengketa",
            "description", "Kelola banding dan resolusi sengketa (FR-RSL-09 s/d 13)",
            "icon", "scale",
            "color", "bg-orange-100 text-orange-600",
            "href", "/admin/sengketa"
        ));
        model.addAttribute("panels", panels);

        // Queue reports (for "Antrean Laporan" tab)
        List<Map<String, Object>> queueReports = getAdminValidationList();
        DateTimeFormatter queueFmt = DateTimeFormatter.ofPattern("dd MMM yyyy");
        queueReports.sort(Comparator.comparing(r -> {
            try { return LocalDate.parse((String) r.get("tanggalMasuk"), queueFmt); }
            catch (Exception e) { return LocalDate.MIN; }
        }));
        model.addAttribute("queueReports", queueReports);

        // Reports for validation tab
        List<Map<String, Object>> validationReports = getAdminValidationList();
        model.addAttribute("validationReports", validationReports);
        
        Map<String, Object> selected = null;
        if (id != null) {
            selected = validationReports.stream()
                    .filter(r -> id.equals(String.valueOf(r.get("id"))))
                    .findFirst().orElse(null);
        }
        model.addAttribute("selectedReport", selected);

        // Merge tickets — from DB reports
        List<Map<String, Object>> mergeTickets = getAdminValidationList();
        mergeTickets = mergeTickets.stream().filter(t -> "Diterima".equals(t.get("status"))).collect(Collectors.toList());
        model.addAttribute("mergeTickets", mergeTickets);
        model.addAttribute("clusters", new ArrayList<>());
        model.addAttribute("selectedTickets", new ArrayList<>());
        model.addAttribute("hiddenChildCount", 0);

        // Disposisi
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

    /** GET /admin/dashboard/detail — halaman detail laporan antrian untuk validasi */
    @GetMapping("/admin/dashboard/detail")
    public String adminQueueDetail(
            Model model,
            @RequestParam(value = "id", required = false) String id
    ) {
        Map<String, Object> selectedReport = null;
        boolean isInDisposisi = false;
        
        if (id != null && !id.trim().isEmpty()) {
            // Check validation queue
            List<Map<String, Object>> validationReports = getAdminValidationList();
            for (Map<String, Object> r : validationReports) {
                if (id.trim().equals(String.valueOf(r.get("id")))) {
                    selectedReport = r;
                    break;
                }
            }
            // Check disposisi if not in validation
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

    /** GET /admin/dashboard/disposisi-detail — halaman detail disposisi untuk edit */
    @GetMapping("/admin/dashboard/disposisi-detail")
    public String adminDisposisiDetail(
            Model model,
            @RequestParam(value = "id", required = false) String id
    ) {
        Map<String, Object> selectedDisposition = null;
        if (id != null) {
            Report r = reportService.findById(id.trim()).orElse(null);
            if (r != null && r.getStatus() == ReportStatus.DIVALIDASI) {
                selectedDisposition = new HashMap<>();
                selectedDisposition.put("id", r.getReportId());
                selectedDisposition.put("judul", r.getTicketNumber() != null ? r.getTicketNumber() : "Laporan");
                selectedDisposition.put("kategori", r.getCategory() != null ? r.getCategory().getCategoryName() : "Lainnya");
                selectedDisposition.put("pelapor", r.getReporter() != null ? r.getReporter().getFullName() : "-");
                selectedDisposition.put("wilayah", r.getLocationHint() != null ? r.getLocationHint() : "-");
                selectedDisposition.put("status", "Tervalidasi");
                selectedDisposition.put("prioritasSistem", "Sedang");
                selectedDisposition.put("dinasRekomendasi", "Dinas Terkait");
                selectedDisposition.put("foto", r.getPhotoBase64() != null ? r.getPhotoBase64() : dummyReportImage());
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

    /**
     * GET /admin/disposisi-detail — route langsung sesuai spec Anggota 3
     * (Fix: sebelumnya hanya tersedia di /admin/dashboard/disposisi-detail)
     */
    @GetMapping("/admin/disposisi-detail")
    public String adminDisposisiDetailDirect(
            Model model,
            @RequestParam(value = "id", required = false) String id
    ) {
        return adminDisposisiDetail(model, id);  // delegate ke method yang sudah ada
    }

    /** GET /admin/laporan-queue — antrean laporan masuk */
    @GetMapping("/admin/laporan-queue")
    public String adminLaporanQueue(Model model) {
        List<Map<String, Object>> reports = getAdminValidationList();
        // Sort FIFO: laporan paling lama (tanggal masuk paling awal) di atas
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy");
        reports.sort(Comparator.comparing(r -> {
            try { return LocalDate.parse((String) r.get("tanggalMasuk"), fmt); }
            catch (Exception e) { return LocalDate.MIN; }
        }));
        model.addAttribute("queueReports", reports);
        return "admin/laporan-queue";
    }

    /** GET /admin/validation — panel validasi laporan */
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
            Report r = reportService.updateStatus(ticketId, approved ? ReportStatus.DIVALIDASI : ReportStatus.DITOLAK, note, note);
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
    } catch (Exception ignored) {
    }
    return "redirect:/admin/validation" + (ticketId != null ? "?id=" + ticketId : "");
}

    // Alias lama agar tidak break
    @GetMapping("/admin/validation-panel")
    public String adminValidationPanelAlias(Model model,
                                            @RequestParam(value = "id", required = false) String id) {
        return adminValidationPanel(model, id);
    }

    @PostMapping("/admin/validation-panel")
    public String adminValidationPanelAliasPost(
            @RequestParam(value = "id", required = false) String id
    ) {
        return "redirect:/admin/validation" + (id != null ? "?id=" + id : "");
    }

/** GET /admin/merge — panel merge tiket duplikat */
@GetMapping("/admin/merge")
public String adminMergeTicketPanel(Model model) {
    List<Map<String, Object>> mergeTickets = getAdminValidationList();
    mergeTickets = mergeTickets.stream().filter(t -> "Diterima".equals(t.get("status"))).collect(Collectors.toList());
    model.addAttribute("mergeTickets", mergeTickets);
    model.addAttribute("clusters", new ArrayList<>());
    model.addAttribute("selectedTickets", new ArrayList<>());
    return "admin/merge-ticket-panel";
}

// Alias lama
@GetMapping("/admin/merge-ticket-panel")
public String adminMergeTicketPanelAlias(Model model) {
    return adminMergeTicketPanel(model);
}

    @PostMapping("/admin/merge")
    public String adminMergeTicketPost(
            @RequestParam(value = "selectedTickets", required = false) List<String> selectedTickets,
            @RequestParam(value = "clusterIndex", required = false) Integer clusterIndex,
            @RequestParam(value = "primaryTicket", required = false) String primaryTicket,
            @RequestParam(value = "mergeReason", required = false) String mergeReason
    ) {
        if (selectedTickets != null && selectedTickets.size() >= 2 && mergeReason != null && !mergeReason.trim().isEmpty()) {
            if (mergeReason.trim().length() < 10) {
                return "redirect:/admin/dashboard?tab=merge&mergeError=shortReason";
            }
            if (primaryTicket == null || primaryTicket.trim().isEmpty()) {
                return "redirect:/admin/dashboard?tab=merge&mergeError=noParent";
            }
            if (!selectedTickets.contains(primaryTicket)) {
                return "redirect:/admin/dashboard?tab=merge&mergeError=invalidParent";
            }

            try {
                String adminId = userService.getUserByEmail("admin@aduaja.go.id")
                        .map(User::getUserId).orElse(null);
                for (String childId : selectedTickets) {
                    if (!childId.equals(primaryTicket)) {
                        mergeRecordService.createMerge(primaryTicket, childId, adminId, mergeReason);
                    }
                }
            } catch (Exception ignored) {
            }
            return "redirect:/admin/dashboard?tab=merge&merged=true";
        }

        if (clusterIndex != null) {
            try {
                List<MergeRecord> records = mergeRecordService.getActiveMergeRecords();
                int idx = 0;
                for (MergeRecord mr : records) {
                    if (idx == clusterIndex) {
                        mergeRecordService.undoMerge(mr.getMergeId());
                        break;
                    }
                    idx++;
                }
            } catch (Exception ignored) {
            }
            return "redirect:/admin/dashboard?tab=merge&separated=true";
        }
        return "redirect:/admin/dashboard?tab=merge";
    }

    @PostMapping("/admin/merge-ticket-panel")
    public String adminMergeTicketPanelAliasPost() {
        return "redirect:/admin/dashboard?tab=merge";
    }

    /** GET /admin/disposisi — panel disposisi ke dinas */
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
        } else {
            reports = new ArrayList<>();
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
        } catch (Exception ignored) {}
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

    // ENCAPSULATION: bungkus data form ke dalam DispositionDTO sebelum dikirim ke Service
    // Controller tidak langsung memanggil repository — semua lewat DTO + Service (Abstraction)
    DispositionDTO dto = new DispositionDTO();
    dto.setReportId(ticketId);
    dto.setTargetAgencyId(dinasId);
    dto.setNotes(catatan);

    try {
        if (dto.getReportId() != null && !dto.getReportId().isEmpty()) {
            // Ambil admin ID dari session atau fallback ke email default
            String adminId = (String) session.getAttribute("userId");
            if (adminId == null) {
                adminId = userService.getUserByEmail("admin@aduaja.go.id")
                        .map(User::getUserId).orElse(null);
            }
            dto.setDispatchedById(adminId);

            // Abstraction: Controller hanya tahu interface, detail disembunyikan di ServiceImpl
            dispositionService.createDisposition(
                    dto.getReportId(),
                    dto.getDispatchedById(),
                    dto.getTargetAgencyId(),
                    dto.getNotes()
            );
            reportService.updateStatus(dto.getReportId(), Report.ReportStatus.DIDISPOSISI);
        }
    } catch (Exception ignored) {
    }

    return "redirect:/admin/disposisi" + (ticketId != null ? "?id=" + ticketId : "");
}

    // Alias lama
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

/** GET /admin/sengketa — panel resolusi sengketa */
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
        m.put("pelapor", d.getReport() != null && d.getReport().getReporter() != null ? d.getReport().getReporter().getFullName() : "-");
        m.put("tanggalLaporan", "-");
        m.put("tanggalSelesai", d.getResolvedAt() != null ? d.getResolvedAt().format(fmt) : "-");
        m.put("statusSebelum", "Selesai");
        m.put("alasanSengketa", d.getReasonText() != null ? d.getReasonText() : "-");
        m.put("fotoBuktiSengketa", dummyReportImage());
        m.put("fotoBuktiPerbaikan", dummyReportImage());
        m.put("keteranganDinas", d.getResolutionNotes() != null ? d.getResolutionNotes() : "-");
        m.put("dinas", "Dinas Terkait");
        m.put("petugasId", "-");
        m.put("petugasNama", "-");
        return m;
    }).collect(Collectors.toList());
    model.addAttribute("disputes", disputes);

    Map<String, Object> selected = null;
    if (id != null && !disputes.isEmpty()) {
        selected = disputes.stream()
                .filter(d -> d.get("id").equals(id))
                .findFirst().orElse(disputes.get(0));
    } else if (!disputes.isEmpty()) {
        selected = disputes.get(0);
    }
    model.addAttribute("selectedDispute", selected);
    return "admin/sengketa-panel";
}

/** POST /admin/sengketa — proses keputusan sengketa */
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
        disputeService.resolveDispute(id, adminId, resolution, catatan);
    } catch (Exception ignored) {
    }
    return "redirect:/admin/sengketa" + (id != null ? "?id=" + id : "");
}

// Alias lama
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
    // ADMIN ROUTES — ADMIN DINAS
    // ==========================================

    /** GET /admin/dinas/dashboard — dashboard admin dinas */
    @GetMapping("/admin/dinas/dashboard")
    public String adminDinasDashboard(Model model) {
        model.addAttribute("dinasName", "Dinas Pekerjaan Umum");

        long diterima = reportService.countByStatus(Report.ReportStatus.DIDISPOSISI);
        long diproses = fieldTaskService.countByStatus(FieldTask.TaskStatus.SEDANG_DIKERJAKAN);
        long selesai = fieldTaskService.countByStatus(FieldTask.TaskStatus.SELESAI);
        long baru = fieldTaskService.countByStatus(FieldTask.TaskStatus.BARU);
        List<Map<String, Object>> stats = new ArrayList<>();
        stats.add(Map.of("title", "Laporan Diterima", "value", diterima,
                "icon", "inbox", "bgColor", "bg-blue-100", "color", "text-blue-600"));
        stats.add(Map.of("title", "Tugas Baru", "value", baru,
                "icon", "inbox", "bgColor", "bg-indigo-100", "color", "text-indigo-600"));
        stats.add(Map.of("title", "Dalam Penanganan", "value", diproses,
                "icon", "wrench", "bgColor", "bg-yellow-100", "color", "text-yellow-600"));
        stats.add(Map.of("title", "Selesai", "value", selesai,
                "icon", "check-circle", "bgColor", "bg-green-100", "color", "text-green-600"));
        model.addAttribute("stats", stats);

        // Penugasan data — real dispositions without assigned tasks
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

        return "admin/dinas/dinas-dashboard";
    }

    // Alias path lama
    @GetMapping("/admin/dinas/dinas-dashboard")
    public String adminDinasDashboardAlias(Model model) {
        return adminDinasDashboard(model);
    }

    /** GET /admin/dinas/queue — antrean laporan yang sudah didisposisikan ke dinas */
    @GetMapping("/admin/dinas/queue")
    public String adminDinasQueue(
            Model model,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page
    ) {
        model.addAttribute("dinasName", "Dinas Pekerjaan Umum");

        List<Map<String, Object>> laporanDinas = new ArrayList<>();

        List<Disposition> realDispositions = dispositionService.getAllDispositions();
        if (!realDispositions.isEmpty()) {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy");
            for (Disposition d : realDispositions) {
                Map<String, Object> m = new HashMap<>();
                m.put("id", d.getReport() != null ? d.getReport().getReportId() : "-");
                m.put("judul", d.getReport() != null ? (d.getReport().getTicketNumber() != null ? d.getReport().getTicketNumber() : "Laporan") : "Disposisi");
                m.put("kategori", d.getReport() != null && d.getReport().getCategory() != null
                    ? d.getReport().getCategory().getCategoryName() : "Lainnya");
                m.put("pelapor", d.getReport() != null && d.getReport().getReporter() != null
                    ? d.getReport().getReporter().getFullName() : "-");
                m.put("wilayah", d.getReport() != null && d.getReport().getLocationHint() != null
                    ? d.getReport().getLocationHint() : "-");
                m.put("tanggalDisposisi", d.getDispatchedAt() != null ? d.getDispatchedAt().format(fmt) : "-");
                m.put("status", "Belum Ditindaklanjuti");
                m.put("prioritas", "Sedang");
                m.put("sisaWaktu", "-");
                laporanDinas.add(m);
            }
        } else {
            laporanDinas = new ArrayList<>();
        }

        long terlambatCount = laporanDinas.stream()
                .filter(r -> "Terlambat SLA".equals(r.get("status"))).count();

        int pageSize = 10;
        int totalCount = laporanDinas.size();
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
        int startIndex = (page - 1) * pageSize + 1;
        int endIndex = Math.min(page * pageSize, totalCount);

        model.addAttribute("laporanDinas", laporanDinas);
        model.addAttribute("terlambatCount", (int) terlambatCount);
        model.addAttribute("page", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("startIndex", startIndex);
        model.addAttribute("endIndex", endIndex);
        return "admin/dinas/dinas-queue";
    }

    // Alias path lama
    @GetMapping("/admin/dinas/dinas-queue")
    public String adminDinasQueueAlias(Model model,
                                       @RequestParam(value = "page", required = false, defaultValue = "1") int page) {
        return adminDinasQueue(model, page);
    }

    /** GET /admin/dinas/penugasan — penugasan petugas lapangan */
    @GetMapping("/admin/dinas/penugasan")
    public String adminDinasPenugasan(
            Model model,
            @RequestParam(value = "id", required = false) String id
    ) {
        List<Map<String, Object>> incomingReports = new ArrayList<>();
        List<Disposition> allDisp = dispositionService.getAllDispositions();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy");
        for (Disposition d : allDisp) {
            if (d.getReport() != null) {
                List<FieldTask> existing = fieldTaskService.getTasksByReport(d.getReport().getReportId());
                if (existing.isEmpty()) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", d.getReport().getReportId());
                    m.put("judul", d.getReport().getTicketNumber() != null ? d.getReport().getTicketNumber() : "Laporan");
                    m.put("kategori", d.getReport().getCategory() != null ? d.getReport().getCategory().getCategoryName() : "Lainnya");
                    m.put("prioritas", "Sedang");
                    m.put("tanggalDisposisi", d.getDispatchedAt() != null ? d.getDispatchedAt().format(fmt) : "-");
                    m.put("wilayah", d.getReport().getLocationHint() != null ? d.getReport().getLocationHint() : "-");
                    m.put("deadline", "-");
                    m.put("instruksiAdmin", d.getNotes() != null ? d.getNotes() : "-");
                    m.put("foto", d.getReport().getPhotoBase64() != null ? d.getReport().getPhotoBase64() : dummyReportImage());
                    incomingReports.add(m);
                }
            }
        }

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
        model.addAttribute("incomingReports", incomingReports);
        model.addAttribute("petugasList", petugasList);

        Map<String, Object> selected = null;
        if (id != null && !id.trim().isEmpty()) {
            String targetId = id.trim();
            for (Map<String, Object> r : incomingReports) {
                Object rid = r.get("id");
                if (rid != null && targetId.equals(String.valueOf(rid))) {
                    selected = r;
                    break;
                }
            }
            if (selected == null && !incomingReports.isEmpty()) {
                selected = incomingReports.get(0);
            }
        } else if (!incomingReports.isEmpty()) {
            selected = incomingReports.get(0);
        }
        model.addAttribute("selectedReport", selected);
        return "admin/dinas/penugasan-petugas";
    }

    /** POST /admin/dinas/penugasan — simpan penugasan petugas */
    @PostMapping("/admin/dinas/penugasan")
    public String adminDinasPenugasanPost(
            @RequestParam(value = "id", required = false) String id,
            @RequestParam(value = "petugasId", required = false) String petugasId,
            @RequestParam(value = "catatan", required = false) String catatan
    ) {
        try {
            String adminDinasId = userService.getUserByEmail("admin.pu@aduaja.go.id")
                    .map(User::getUserId).orElse(null);
            fieldTaskService.createTask(id, petugasId, adminDinasId);
        } catch (Exception ignored) {
        }
        return "redirect:/admin/dinas/penugasan" + (id != null ? "?id=" + id : "");
    }

    // Alias path lama
    @GetMapping("/admin/dinas/penugasan-petugas")
    public String adminDinasPenugasanAlias(Model model,
                                           @RequestParam(value = "id", required = false) String id) {
        return adminDinasPenugasan(model, id);
    }

    @PostMapping("/admin/dinas/penugasan-petugas")
    public String adminDinasPenugasanAliasPost(
            @RequestParam(value = "id", required = false) String id
    ) {
        return "redirect:/admin/dinas/penugasan" + (id != null ? "?id=" + id : "");
    }

    /** GET /admin/dinas/progress — update progress penanganan */
    @GetMapping("/admin/dinas/progress")
    public String adminDinasProgress(
            Model model,
            @RequestParam(value = "id", required = false) String id
    ) {
        List<Map<String, Object>> ticketsInProgress = new ArrayList<>();
        List<FieldTask> realTasks = fieldTaskService.getTasksByStatus(FieldTask.TaskStatus.SEDANG_DIKERJAKAN);
        if (!realTasks.isEmpty()) {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy");
            for (FieldTask t : realTasks) {
                Map<String, Object> m = new HashMap<>();
                m.put("id", t.getTaskId());
                m.put("judul", t.getReport() != null ? (t.getReport().getTicketNumber() != null ? t.getReport().getTicketNumber() : "Laporan") : "Tugas");
                m.put("kategori", t.getReport() != null && t.getReport().getCategory() != null
                    ? t.getReport().getCategory().getCategoryName() : "Lainnya");
                m.put("prioritas", "Sedang");
                m.put("pelapor", t.getReport() != null && t.getReport().getReporter() != null
                    ? t.getReport().getReporter().getFullName() : "-");
                m.put("deadline", "-");
                m.put("foto", dummyReportImage());
                List<Map<String, Object>> ph = new ArrayList<>();
                if (t.getStartedAt() != null) {
                    ph.add(Map.of("tanggal", t.getStartedAt().format(fmt), "petugas",
                        t.getOfficer() != null ? t.getOfficer().getFullName() : "-",
                        "keterangan", "Pengerjaan dimulai", "estimasi", "-"));
                }
                m.put("progressHistory", ph);
                ticketsInProgress.add(m);
            }
        } else {
            ticketsInProgress = new ArrayList<>();
        }
        model.addAttribute("ticketsInProgress", ticketsInProgress);

        Map<String, Object> selected = null;
        if (id != null) {
            selected = ticketsInProgress.stream()
                    .filter(t -> t.get("id").equals(id))
                    .findFirst().orElse(ticketsInProgress.isEmpty() ? null : ticketsInProgress.get(0));
        }
        model.addAttribute("selectedTicket", selected);
        return "admin/dinas/progress-update";
    }

    /** POST /admin/dinas/progress — simpan update progress */
    @PostMapping("/admin/dinas/progress")
    public String adminDinasProgressPost(
            @RequestParam(value = "id", required = false) String id,
            @RequestParam(value = "keterangan", required = false) String keterangan,
            @RequestParam(value = "estimasi", required = false) String estimasi
    ) {
        try {
            fieldTaskService.startTask(id, null, null);
        } catch (Exception ignored) {
        }
        return "redirect:/admin/dinas/progress" + (id != null ? "?id=" + id : "");
    }

    // Alias path lama
    @GetMapping("/admin/dinas/progress-update")
    public String adminDinasProgressAlias(Model model,
                                          @RequestParam(value = "id", required = false) String id) {
        return adminDinasProgress(model, id);
    }

    @PostMapping("/admin/dinas/progress-update")
    public String adminDinasProgressAliasPost(
            @RequestParam(value = "id", required = false) String id
    ) {
        return "redirect:/admin/dinas/progress" + (id != null ? "?id=" + id : "");
    }

    /** GET /admin/dinas/close — penutupan tiket + upload bukti */
    @GetMapping("/admin/dinas/close")
    public String adminDinasClose(
            Model model,
            @RequestParam(value = "id", required = false) String id
    ) {
        List<Map<String, Object>> ticketsReady = new ArrayList<>();
        List<FieldTask> realTasks = fieldTaskService.getTasksByStatus(FieldTask.TaskStatus.SELESAI);
        if (!realTasks.isEmpty()) {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy");
            for (FieldTask t : realTasks) {
                Map<String, Object> m = new HashMap<>();
                m.put("id", t.getTaskId());
                m.put("judul", t.getReport() != null ? (t.getReport().getTicketNumber() != null ? t.getReport().getTicketNumber() : "Laporan") : "Tugas");
                m.put("kategori", t.getReport() != null && t.getReport().getCategory() != null
                    ? t.getReport().getCategory().getCategoryName() : "Lainnya");
                m.put("prioritas", "Sedang");
                m.put("pelapor", t.getReport() != null && t.getReport().getReporter() != null
                    ? t.getReport().getReporter().getFullName() : "-");
                m.put("wilayah", t.getReport() != null && t.getReport().getLocationHint() != null
                    ? t.getReport().getLocationHint() : "-");
                m.put("foto", dummyReportImage());
                List<Map<String, Object>> ph = new ArrayList<>();
                if (t.getStartedAt() != null) {
                    ph.add(Map.of("tanggal", t.getStartedAt().format(fmt), "keterangan", "Pengerjaan dimulai"));
                }
                if (t.getCompletedAt() != null) {
                    ph.add(Map.of("tanggal", t.getCompletedAt().format(fmt), "keterangan", "Pengerjaan selesai"));
                }
                m.put("progressHistory", ph);
                ticketsReady.add(m);
            }
        } else {
            ticketsReady = new ArrayList<>();
        }
        model.addAttribute("ticketsReadyToClose", ticketsReady);

        Map<String, Object> selected = null;
        if (id != null) {
            selected = ticketsReady.stream()
                    .filter(t -> t.get("id").equals(id))
                    .findFirst().orElse(ticketsReady.isEmpty() ? null : ticketsReady.get(0));
        }
        model.addAttribute("selectedTicket", selected);
        return "admin/dinas/close-ticket";
    }

    /** POST /admin/dinas/close — proses tutup tiket */
    @PostMapping("/admin/dinas/close")
    public String adminDinasClosePost(
            @RequestParam(value = "id", required = false) String id,
            @RequestParam(value = "keterangan", required = false) String keterangan
    ) {
        try {
            fieldTaskService.completeTask(id);
        } catch (Exception ignored) {
        }
        return "redirect:/admin/dinas/close" + (id != null ? "?id=" + id : "");
    }

    // Alias path lama
    @GetMapping("/admin/dinas/close-ticket")
    public String adminDinasCloseAlias(Model model,
                                       @RequestParam(value = "id", required = false) String id) {
        return adminDinasClose(model, id);
    }

    @PostMapping("/admin/dinas/close-ticket")
    public String adminDinasCloseAliasPost(
            @RequestParam(value = "id", required = false) String id
    ) {
        return "redirect:/admin/dinas/close" + (id != null ? "?id=" + id : "");
    }

    /** GET /admin/dinas/sengketa — panel resolusi sengketa untuk admin dinas */
    @GetMapping("/admin/dinas/sengketa")
    public String adminDinasSengketa(
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
            m.put("pelapor", d.getReport() != null && d.getReport().getReporter() != null ? d.getReport().getReporter().getFullName() : "-");
            m.put("tanggalLaporan", "-");
            m.put("tanggalSelesai", d.getResolvedAt() != null ? d.getResolvedAt().format(fmt) : "-");
            m.put("statusSebelum", "Selesai");
            m.put("alasanSengketa", d.getReasonText() != null ? d.getReasonText() : "-");
            m.put("fotoBuktiSengketa", dummyReportImage());
            m.put("fotoBuktiPerbaikan", dummyReportImage());
            m.put("keteranganDinas", d.getResolutionNotes() != null ? d.getResolutionNotes() : "-");
            m.put("dinas", "Dinas Terkait");
            m.put("petugasId", "-");
            m.put("petugasNama", "-");
            return m;
        }).collect(Collectors.toList());
        model.addAttribute("disputes", disputes);

        Map<String, Object> selected = null;
        List<Map<String, Object>> availablePetugas = new ArrayList<>();
        if (id != null && !id.trim().isEmpty()) {
            String targetId = id.trim();
            for (Map<String, Object> d : disputes) {
                Object did = d.get("id");
                if (did != null && targetId.equals(String.valueOf(did))) {
                    selected = d;
                    break;
                }
            }
            if (selected == null && !disputes.isEmpty()) {
                selected = disputes.get(0);
            }
        } else if (!disputes.isEmpty()) {
            selected = disputes.get(0);
        }

        if (selected != null) {
            String originalPetugasId = (String) selected.get("petugasId");
            List<User> realPetugas = userService.findByRole(User.Role.PETUGAS);
            for (User p : realPetugas) {
                if (!p.getUserId().equals(originalPetugasId)) {
                    Map<String, Object> pm = new HashMap<>();
                    pm.put("id", p.getUserId());
                    pm.put("nama", p.getFullName());
                    availablePetugas.add(pm);
                }
            }
            model.addAttribute("availablePetugasForReassignment", availablePetugas);
            model.addAttribute("originalPetugasId", originalPetugasId);
            model.addAttribute("originalPetugasNama", selected.get("petugasNama"));
        }

        model.addAttribute("selectedDispute", selected);
        return "admin/dinas/sengketa-dinas";
    }

    /** POST /admin/dinas/sengketa — proses keputusan sengketa untuk admin dinas */
    @PostMapping("/admin/dinas/sengketa")
    public String adminDinasSengketaPost(
            @RequestParam(value = "id", required = false) String id,
            @RequestParam(value = "keputusan", required = false) String keputusan,
            @RequestParam(value = "catatan", required = false) String catatan,
            @RequestParam(value = "petugasId", required = false) String petugasId,
            HttpSession session
    ) {
        // FIX: implementasi nyata, bukan hanya stub/redirect kosong
        String adminId = (String) session.getAttribute("userId");
        if (adminId == null) {
            adminId = userService.getUserByEmail("admin.pu@aduaja.go.id")
                    .map(User::getUserId).orElse(null);
        }

        try {
            if ("diterima".equals(keputusan) && petugasId != null && !petugasId.isEmpty()) {
                // Sengketa DITERIMA → tugaskan kembali ke petugas lain
                // 1. Resolve dispute dengan status TUGASKAN_KEMBALI
                if (adminId != null) {
                    disputeService.resolveDispute(id, adminId,
                            DisputeRecord.ResolutionType.TUGASKAN_KEMBALI, catatan);
                }
                // 2. Cari tugas lapangan yang terkait & reassign ke petugas baru
                //    (Polymorphism: memanggil reassignTask via FieldTaskService interface)
                try {
                    DisputeRecord dispute = disputeService.getAllDisputes().stream()
                            .filter(d -> id.equals(d.getDisputeId()))
                            .findFirst().orElse(null);
                    if (dispute != null && dispute.getReport() != null) {
                        String reportId = dispute.getReport().getReportId();
                        List<FieldTask> relatedTasks = fieldTaskService.getTasksByReport(reportId);
                        for (FieldTask task : relatedTasks) {
                            if (task.getTaskStatus() != FieldTask.TaskStatus.SELESAI) {
                                fieldTaskService.reassignTask(task.getTaskId(), petugasId);
                            }
                        }
                    }
                } catch (Exception ignored) {}

                return "redirect:/admin/dinas/sengketa?reassigned=true";

            } else {
                // Sengketa DITOLAK → tutup laporan
                if (adminId != null) {
                    disputeService.resolveDispute(id, adminId,
                            DisputeRecord.ResolutionType.TUTUP_LAPORAN, catatan);
                }
            }
        } catch (Exception ignored) {}

        return "redirect:/admin/dinas/sengketa" + (id != null ? "?id=" + id : "");
    }

    // ==========================================
    // PETUGAS ROUTES
    // ==========================================


    // GET /petugas/login — Ditangani oleh AdminAuthController
    // POST /petugas/login — Ditangani oleh AdminAuthController


    /** GET /petugas/home — redirect ke dashboard */
    @GetMapping("/petugas/home")
    public String petugasHome() {
        return "redirect:/petugas/dashboard";
    }

    /** POST /petugas/dashboard — proses check-in/check-out/istirahat */
    @PostMapping("/petugas/dashboard")
    public String petugasDashboardPost(
            @RequestParam(value = "checkIn", required = false) Boolean checkIn,
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "attendanceId", required = false) String attendanceId,
            @RequestParam(value = "latitude", required = false) java.math.BigDecimal latitude,
            @RequestParam(value = "longitude", required = false) java.math.BigDecimal longitude,
            @RequestParam(value = "deviceInfo", required = false) String deviceInfo,
            HttpSession session
    ) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) return "redirect:/petugas/login";

        try {
            if (checkIn != null && checkIn) {
                attendanceService.checkIn(userId, latitude, longitude, deviceInfo);
            } else if ("checkout".equals(action) && attendanceId != null) {
                attendanceService.checkOut(attendanceId);
            } else if ("break".equals(action) && attendanceId != null) {
                attendanceService.setBreak(attendanceId);
            } else if ("resume".equals(action) && attendanceId != null) {
                attendanceService.resumeFromBreak(attendanceId);
            }
        } catch (Exception ignored) {}

        return "redirect:/petugas/dashboard";
    }

    /** POST /petugas/task-action — proses aksi pada tugas (Start, Complete, Postpone, Reassign) */
    @PostMapping("/petugas/task-action")
    public String petugasTaskAction(
            @RequestParam(value = "id", required = false) String id,
            @RequestParam(value = "action", required = false, defaultValue = "start") String action,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "newOfficerId", required = false) String newOfficerId,
            @RequestParam(value = "latitude", required = false) java.math.BigDecimal latitude,
            @RequestParam(value = "longitude", required = false) java.math.BigDecimal longitude,
            HttpSession session
    ) {
        if (id != null) {
            try {
                // Polymorphism: switch ke berbagai implementasi service method
                switch (action) {
                    case "start" ->
                        // startTask(taskId, lat, lon) — Overloading: method yang sama dipanggil berbeda
                        fieldTaskService.startTask(id, latitude, longitude);

                    case "complete" ->
                        // completeTask(taskId) — Override dari FieldTaskService interface
                        fieldTaskService.completeTask(id);

                    case "postpone" -> {
                        // FIX: handle postpone — sekarang juga menyimpan TaskPostponement record
                        String reason = description != null && !description.isBlank()
                                ? description : "Ditunda oleh petugas";
                        fieldTaskService.postponeTask(id, reason);
                    }

                    case "reassign" -> {
                        // FIX: handle reassign — tugaskan ke petugas lain
                        String targetOfficer = newOfficerId != null ? newOfficerId : "";
                        if (!targetOfficer.isBlank()) {
                            fieldTaskService.reassignTask(id, targetOfficer);
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return "redirect:/petugas/task-detail?id=" + id;
    }

    @GetMapping("/petugas/dashboard")
    public String petugasDashboard(
            Model model,
            HttpSession session,
            @RequestParam(value = "checkIn", required = false) Boolean checkIn
    ) {
        String userId = (String) session.getAttribute("userId");

        boolean isCheckedIn = true;
        if (checkIn != null && !checkIn) isCheckedIn = false;
        Map<String, Object> defUser = new HashMap<>();
        defUser.put("name", "Ahmad Fauzi");
        defUser.put("dinas", "Dinas Pekerjaan Umum");
        model.addAttribute("user", defUser);
        Map<String, Object> defAttendance = new HashMap<>();
        defAttendance.put("attendanceId", "-");
        defAttendance.put("checkedIn", isCheckedIn);
        defAttendance.put("currentStatus", isCheckedIn ? "Siap Bertugas" : "Belum Check-In");
        defAttendance.put("checkInTime", isCheckedIn ? "08:05" : "-");
        defAttendance.put("workDuration", isCheckedIn ? "04:32:10" : "00:00:00");
        defAttendance.put("location", isCheckedIn ? "Kantor Dinas PU Medan" : "-");
        model.addAttribute("attendance", defAttendance);
        List<Map<String, Object>> defActive = new ArrayList<>();
        long selesai = 0, inProgress = 0, newTasks = 0, pending = 0;

        if (userId != null) {
            userService.findById(userId).ifPresent(officer -> {
                model.addAttribute("user", Map.of("name", officer.getFullName(), "dinas", "Dinas Pekerjaan Umum"));
                List<FieldTask> realTasks = fieldTaskService.getTasksByOfficer(userId);
                if (!realTasks.isEmpty()) {
                    long s = realTasks.stream().filter(t -> t.getTaskStatus() == TaskStatus.SELESAI).count();
                    long ip = realTasks.stream().filter(t -> t.getTaskStatus() == TaskStatus.SEDANG_DIKERJAKAN).count();
                    long n = realTasks.stream().filter(t -> t.getTaskStatus() == TaskStatus.BARU).count();
                    long p = realTasks.stream().filter(t -> t.getTaskStatus() == TaskStatus.TERTUNDA).count();
                    model.addAttribute("stats", Map.of("selesaiHariIni", s, "sedangDikerjakan", ip, "tugasBaru", n, "tertunda", p));
                    List<Map<String, Object>> realActive = realTasks.stream().limit(5).map(this::toPetugasTaskMap).collect(Collectors.toList());
                    if (!realActive.isEmpty()) model.addAttribute("activeTasks", realActive);
                }
                attendanceService.getCurrentShift(userId).ifPresent(shift -> {
                    model.addAttribute("attendance", Map.of(
                        "attendanceId", shift.getAttendanceId(),
                        "checkedIn", shift.getCheckInAt() != null,
                        "currentStatus", shift.getShiftStatus() == OfficerAttendance.ShiftStatus.AKTIF ? "Siap Bertugas"
                            : shift.getShiftStatus() == OfficerAttendance.ShiftStatus.ISTIRAHAT ? "Istirahat" : "Selesai Shift",
                        "checkInTime", shift.getCheckInAt() != null ? shift.getCheckInAt().format(DateTimeFormatter.ofPattern("HH:mm")) : "-",
                        "workDuration", shift.getCheckInAt() != null ? formatDuration(Duration.between(shift.getCheckInAt(), LocalDateTime.now())) : "00:00:00",
                        "location", shift.getCheckInLatitude() != null ? shift.getCheckInLatitude() + ", " + shift.getCheckInLongitude() : "Kantor Dinas PU Medan"
                    ));
                });
            });
        }
        if (!model.containsAttribute("stats")) {
            model.addAttribute("stats", Map.of("selesaiHariIni", 0L, "sedangDikerjakan", 0L, "tugasBaru", 0L, "tertunda", 0L));
        }
        if (!model.containsAttribute("activeTasks")) {
            model.addAttribute("activeTasks", new ArrayList<>());
        }

        Map<String, Object> deviceInfo = new HashMap<>();
        deviceInfo.put("browser", "Chrome 124");
        deviceInfo.put("os", "Android 14");
        model.addAttribute("deviceInfo", deviceInfo);

        return "petugas/dashboard";
    }

    @GetMapping("/petugas/tasks")
    public String petugasTasks(Model model, HttpSession session) {
        List<Map<String, Object>> tasksNew = new ArrayList<>();
        List<Map<String, Object>> tasksInProgress = new ArrayList<>();
        List<Map<String, Object>> tasksPending = new ArrayList<>();

        String userId = (String) session.getAttribute("userId");
        if (userId != null) {
            List<FieldTask> realTasks = fieldTaskService.getTasksByOfficer(userId);
            if (!realTasks.isEmpty()) {
                for (FieldTask t : realTasks) {
                    switch (t.getTaskStatus()) {
                        case BARU -> tasksNew.add(toPetugasTaskMap(t));
                        case SEDANG_DIKERJAKAN -> tasksInProgress.add(toPetugasTaskMap(t));
                        case TERTUNDA -> tasksPending.add(toPetugasTaskMap(t));
                        default -> {}
                    }
                }
                model.addAttribute("tasksNew", tasksNew);
                model.addAttribute("tasksInProgress", tasksInProgress);
                model.addAttribute("tasksPending", tasksPending);
                return "petugas/tasks";
            }
        }
        model.addAttribute("tasksNew", tasksNew);
        model.addAttribute("tasksInProgress", tasksInProgress);
        model.addAttribute("tasksPending", tasksPending);
        return "petugas/tasks";
    }

    @GetMapping("/petugas/task-detail")
    public String petugasTaskDetail(
            Model model,
            HttpSession session,
            @RequestParam(value = "id", required = false, defaultValue = "TGS-001") String id
    ) {
        String userId = (String) session.getAttribute("userId");
        if (userId != null) {
            Optional<FieldTask> realTask = fieldTaskService.getTaskById(id);
            if (realTask.isPresent()) {
                FieldTask ft = realTask.get();
                Map<String, Object> task = toPetugasTaskMap(ft);
                task.put("reporterPhone", ft.getReport() != null && ft.getReport().getReporter() != null
                    ? ft.getReport().getReporter().getEmail() : "-");

                Map<String, Object> locationMap = new HashMap<>();
                String addr = ft.getReport() != null ? (ft.getReport().getLocationHint() != null ? ft.getReport().getLocationHint() : "-") : "-";
                locationMap.put("address", addr);
                locationMap.put("latitude", ft.getOfficerLatitude() != null ? ft.getOfficerLatitude().toPlainString() : "3.5952");
                locationMap.put("longitude", ft.getOfficerLongitude() != null ? ft.getOfficerLongitude().toPlainString() : "98.6722");
                task.put("location", locationMap);

                List<Map<String, Object>> statusHistory = new ArrayList<>();
                statusHistory.add(Map.of("status", "Tugas Dibuat", "time",
                    ft.getCreatedAt() != null ? ft.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")) : "-",
                    "note", "Tugas diterima dari laporan warga"));
                if (ft.getStartedAt() != null) {
                    statusHistory.add(Map.of("status", "Mulai Dikerjakan", "time",
                        ft.getStartedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")),
                        "note", "Petugas memulai pengerjaan"));
                }
                if (ft.getCompletedAt() != null) {
                    statusHistory.add(Map.of("status", "Selesai", "time",
                        ft.getCompletedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")),
                        "note", "Tugas telah selesai dikerjakan"));
                }
                task.put("statusHistory", statusHistory);

                model.addAttribute("task", task);
                model.addAttribute("user", userId != null ? userService.findById(userId)
                    .map(u -> Map.of("name", u.getFullName())).orElse(Map.of("name", "Petugas"))
                    : Map.of("name", "Petugas"));
                model.addAttribute("attendance", Map.of("checkedIn", true));
                return "petugas/task-detail";
            }
        }

        return "redirect:/petugas/tasks";
    }

    @GetMapping("/petugas/task-execution")
    public String petugasTaskExecution(
            Model model,
            HttpSession session,
            @RequestParam(value = "id", required = false, defaultValue = "TGS-001") String id
    ) {
        String userId = (String) session.getAttribute("userId");
        if (userId != null) {
            Optional<FieldTask> realTask = fieldTaskService.getTaskById(id);
            if (realTask.isPresent()) {
                FieldTask ft = realTask.get();
                Map<String, Object> task = toPetugasTaskMap(ft);
                Map<String, Object> locationMap = new HashMap<>();
                String addr = ft.getReport() != null ? (ft.getReport().getLocationHint() != null ? ft.getReport().getLocationHint() : "-") : "-";
                locationMap.put("address", addr);
                locationMap.put("latitude", ft.getOfficerLatitude() != null ? ft.getOfficerLatitude().toPlainString() : "3.5952");
                locationMap.put("longitude", ft.getOfficerLongitude() != null ? ft.getOfficerLongitude().toPlainString() : "98.6722");
                task.put("location", locationMap);
                task.put("distanceToTask", "-");
                model.addAttribute("task", task);
                model.addAttribute("materials", new ArrayList<>());
                return "petugas/task-execution";
            }
        }

        return "redirect:/petugas/tasks";
    }

    /** POST /petugas/task-execution — simpan laporan pengerjaan tugas */
    @PostMapping("/petugas/task-execution")
    public String petugasTaskExecutionPost(
            @RequestParam(value = "id", required = false, defaultValue = "TGS-001") String id,
            @RequestParam(value = "action", required = false, defaultValue = "save") String action,
            @RequestParam(value = "materialName", required = false) String materialName,
            @RequestParam(value = "quantity", required = false) Integer quantity,
            @RequestParam(value = "unit", required = false) String unit
    ) {
        if ("complete".equals(action)) {
            try {
                fieldTaskService.completeTask(id);
            } catch (Exception ignored) {
            }
            return "redirect:/petugas/dashboard";
        }
        return "redirect:/petugas/task-execution?id=" + id;
    }

    @GetMapping("/petugas/history")
    public String petugasHistory(Model model, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId != null) {
            List<FieldTask> allTasks = fieldTaskService.getTasksByOfficer(userId);
            List<FieldTask> completed = allTasks.stream()
                .filter(t -> t.getTaskStatus() == TaskStatus.SELESAI).collect(Collectors.toList());
            long totalH = completed.stream()
                .filter(t -> t.getStartedAt() != null && t.getCompletedAt() != null)
                .mapToLong(t -> Duration.between(t.getStartedAt(), t.getCompletedAt()).toHours())
                .sum();
            long avgMins = completed.isEmpty() ? 0 :
                completed.stream()
                    .filter(t -> t.getStartedAt() != null && t.getCompletedAt() != null)
                    .mapToLong(t -> Duration.between(t.getStartedAt(), t.getCompletedAt()).toMinutes())
                    .sum() / completed.size();
            model.addAttribute("stats", Map.of(
                "totalTasks", allTasks.size(),
                "avgDuration", (avgMins / 60) + "j " + (avgMins % 60) + "m",
                "totalHours", totalH
            ));
            List<Map<String, Object>> taskList = completed.stream().map(t -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id", t.getTaskId());
                m.put("title", t.getReport() != null ? "Tugas #" + t.getTaskId().substring(0, 8) : "-");
                m.put("category", t.getReport() != null && t.getReport().getCategory() != null
                    ? t.getReport().getCategory().getCategoryName() : "Lainnya");
                m.put("location", t.getReport() != null ? (t.getReport().getLocationHint() != null ? t.getReport().getLocationHint() : "-") : "-");
                m.put("status", "approved");
                m.put("duration", t.getStartedAt() != null && t.getCompletedAt() != null
                    ? formatDuration(Duration.between(t.getStartedAt(), t.getCompletedAt())) : "-");
                m.put("completedAt", t.getCompletedAt() != null
                    ? t.getCompletedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy")) : "-");
                m.put("photoBefore", 0);
                m.put("photoAfter", 0);
                m.put("materialUsed", 0);
                return m;
            }).collect(Collectors.toList());
            if (!taskList.isEmpty()) {
                model.addAttribute("tasks", taskList);
                return "petugas/history";
            }
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTasks", 12);
        stats.put("avgDuration", "2j 18m");
        stats.put("totalHours", 28);
        model.addAttribute("stats", stats);
        List<Map<String, Object>> tasks = new ArrayList<>();
        tasks.add(Map.of("id", "TGS-001", "title", "Perbaikan Jalan Berlubang Jl. Sudirman",
                "category", "Jalan", "location", "Jl. Sudirman, Medan Kota",
                "status", "approved", "duration", "2j 15m", "completedAt", "28 Apr 2025",
                "photoBefore", 2, "photoAfter", 3, "materialUsed", 2));
        tasks.add(Map.of("id", "TGS-002", "title", "Penggantian Lampu Jalan Gang Melati",
                "category", "Penerangan", "location", "Gang Melati, Medan Baru",
                "status", "pending_review", "duration", "1j 45m", "completedAt", "26 Apr 2025",
                "photoBefore", 2, "photoAfter", 2, "materialUsed", 1));
        tasks.add(Map.of("id", "TGS-003", "title", "Pembersihan Saluran Drainase Jl. Gatot Subroto",
                "category", "Drainase", "location", "Jl. Gatot Subroto, Medan Petisah",
                "status", "approved", "duration", "3j 10m", "completedAt", "25 Apr 2025",
                "photoBefore", 3, "photoAfter", 3, "materialUsed", 0));
        model.addAttribute("tasks", tasks);
        return "petugas/history";
    }

    @GetMapping("/petugas/reports")
    public String petugasReports(
            Model model,
            HttpSession session,
            @RequestParam(value = "period", required = false, defaultValue = "week") String period
    ) {
        String userId = (String) session.getAttribute("userId");
        Map<String, Object> user = new HashMap<>();
        user.put("name", "Petugas");
        if (userId != null) {
            userService.findById(userId).ifPresent(u -> user.put("name", u.getFullName()));
        }
        model.addAttribute("user", user);
        model.addAttribute("selectedPeriod", period);

        List<FieldTask> allTasks = userId != null ? fieldTaskService.getTasksByOfficer(userId) : new ArrayList<>();
        List<FieldTask> completed = allTasks.stream().filter(t -> t.getTaskStatus() == TaskStatus.SELESAI).collect(Collectors.toList());
        List<FieldTask> inProgress = allTasks.stream().filter(t -> t.getTaskStatus() == TaskStatus.SEDANG_DIKERJAKAN).collect(Collectors.toList());

        int total = allTasks.size();
        int completedCount = completed.size();
        int pendingCount = allTasks.size() - completedCount;
        long totalMinutes = completed.stream()
            .filter(t -> t.getStartedAt() != null && t.getCompletedAt() != null)
            .mapToLong(t -> Duration.between(t.getStartedAt(), t.getCompletedAt()).toMinutes())
            .sum();
        int hours = total > 0 ? (int) (totalMinutes / 60) : 0;
        long avgMins = completedCount > 0 ? totalMinutes / completedCount : 0;
        String avgDur = avgMins > 0 ? (avgMins / 60) + "j " + (avgMins % 60) + "m" : "-";
        String rate = total > 0 ? (int) Math.round((double) completedCount / total * 100) + "%" : "0%";

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTasks",     total);
        stats.put("completedTasks", completedCount);
        stats.put("pendingTasks",   pendingCount);
        stats.put("totalHours",     hours);
        stats.put("avgDuration",    avgDur);
        stats.put("completionRate", rate);

        Map<String, Long> catCount = new java.util.TreeMap<>();
        for (FieldTask t : allTasks) {
            String cat = t.getReport() != null && t.getReport().getCategory() != null
                ? t.getReport().getCategory().getCategoryName() : "Lainnya";
            catCount.merge(cat, 1L, Long::sum);
        }
        List<Map<String, Object>> categories = new ArrayList<>();
        String[] catColors = {"bg-blue-500", "bg-yellow-500", "bg-green-500", "bg-purple-500", "bg-red-500", "bg-gray-400"};
        int ci = 0;
        for (Map.Entry<String, Long> e : catCount.entrySet()) {
            int pct = total > 0 ? (int) Math.round(e.getValue() * 100.0 / total) : 0;
            categories.add(Map.of("name", e.getKey(), "count", e.getValue().intValue(),
                "percentage", pct, "colorClass", catColors[ci++ % catColors.length]));
        }
        stats.put("categories", categories);

        List<Map<String, Object>> progress = new ArrayList<>();
        String progressTitle;
        LocalDate now = LocalDate.now();
        if (period.equals("week")) {
            progressTitle = "Tugas per Hari (7 Hari Terakhir)";
            String[] dayNames = {"Min","Sen","Sel","Rab","Kam","Jum","Sab"};
            for (int i = 6; i >= 0; i--) {
                LocalDate d = now.minusDays(i);
                String dayLabel = dayNames[d.getDayOfWeek().getValue() % 7];
                int dayTasks = (int) completed.stream()
                    .filter(t -> t.getCompletedAt() != null && t.getCompletedAt().toLocalDate().equals(d))
                    .count();
                progress.add(Map.of("label", dayLabel, "completed", dayTasks, "percent", Math.min(dayTasks * 25, 100)));
            }
        } else if (period.equals("month")) {
            progressTitle = "Tugas per Minggu (30 Hari Terakhir)";
            for (int w = 4; w >= 1; w--) {
                LocalDate start = now.minusWeeks(w);
                LocalDate end = start.plusDays(6);
                int weekTasks = (int) completed.stream()
                    .filter(t -> t.getCompletedAt() != null && !t.getCompletedAt().toLocalDate().isBefore(start) && !t.getCompletedAt().toLocalDate().isAfter(end))
                    .count();
                progress.add(Map.of("label", "Mg " + (5 - w), "completed", weekTasks, "percent", Math.min(weekTasks * 25, 100)));
            }
        } else {
            progressTitle = "Tugas per Bulan (1 Tahun)";
            String[] monthLabels = {"Jan","Feb","Mar","Apr","Mei","Jun","Jul","Agt","Sep","Okt","Nov","Des"};
            for (int m = 0; m < 12; m++) {
                int monthValue = m + 1;
                int monthTasks = (int) completed.stream()
                    .filter(t -> t.getCompletedAt() != null && t.getCompletedAt().getMonthValue() == monthValue)
                    .count();
                progress.add(Map.of("label", monthLabels[m], "completed", monthTasks, "percent", Math.min(monthTasks * 25, 100)));
            }
        }
        stats.put("progress", progress);
        stats.put("progressTitle", progressTitle);

        model.addAttribute("stats", stats);
        model.addAttribute("materialStats", new ArrayList<>());
        return "petugas/reports";
    }

    @GetMapping("/petugas/attendance-history")
    public String petugasAttendanceHistory(Model model, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId != null) {
            List<OfficerAttendance> realRecords = attendanceService.getAttendanceByOfficer(userId);
            if (!realRecords.isEmpty()) {
                long daysPresent = realRecords.size();
                long totalMinutes = realRecords.stream()
                    .filter(r -> r.getCheckInAt() != null && r.getCheckOutAt() != null)
                    .mapToLong(r -> Duration.between(r.getCheckInAt(), r.getCheckOutAt()).toMinutes())
                    .sum();
                long lateCount = 0;
                List<Map<String, Object>> recordList = realRecords.stream().map(r -> {
                    Map<String, Object> di = new HashMap<>();
                    di.put("browser", r.getDeviceInfo() != null ? r.getDeviceInfo() : "Unknown");
                    di.put("os", "Android");
                    di.put("ip", "-");
                    String dateFmt = r.getCheckInAt() != null
                        ? r.getCheckInAt().format(DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy")) : "-";
                    String ciTime = r.getCheckInAt() != null
                        ? r.getCheckInAt().format(DateTimeFormatter.ofPattern("HH:mm")) : "-";
                    String coTime = r.getCheckOutAt() != null
                        ? r.getCheckOutAt().format(DateTimeFormatter.ofPattern("HH:mm")) : null;
                    String dur = r.getCheckInAt() != null && r.getCheckOutAt() != null
                        ? formatDuration(Duration.between(r.getCheckInAt(), r.getCheckOutAt())) : "-";
                    String status = r.getShiftStatus() == OfficerAttendance.ShiftStatus.SELESAI_SHIFT ? "completed" : "ongoing";
                    return buildAttendanceRecord(dateFmt, ciTime, coTime, dur, status,
                        Map.of("address", r.getCheckInLatitude() != null
                            ? r.getCheckInLatitude().toPlainString() + ", " + r.getCheckInLongitude().toPlainString() : "Kantor Dinas"),
                        r.getCheckOutAt() != null
                            ? Map.of("address", "Check-out lokasi")
                            : null,
                        di);
                }).collect(Collectors.toList());
                model.addAttribute("user", Map.of("name", "Petugas"));
                model.addAttribute("summary", Map.of("daysPresent", (int) daysPresent, "totalHours", totalMinutes / 60, "lateCount", (int) lateCount));
                model.addAttribute("attendanceRecords", recordList);
                return "petugas/attendance-history";
            }
        }

        Map<String, Object> user = new HashMap<>();
        user.put("name", "Ahmad Fauzi");
        model.addAttribute("user", user);
        Map<String, Object> summary = new HashMap<>();
        summary.put("daysPresent", 18);
        summary.put("totalHours",  144);
        summary.put("lateCount",   2);
        model.addAttribute("summary", summary);
        List<Map<String, Object>> records = new ArrayList<>();
        records.add(buildAttendanceRecord("Senin, 28 Apr 2025", "08:05", "17:02", "08:57:00", "completed",
                Map.of("address","Jl. Setia Budi No. 1, Medan Sunggal"),
                Map.of("address","Jl. Sudirman No. 10, Medan Kota"),
                Map.of("browser","Chrome 124","os","Android 14","ip","180.244.12.34")));
        records.add(buildAttendanceRecord("Jumat, 25 Apr 2025", "08:15", "17:00", "08:45:00", "completed",
                Map.of("address","Jl. Setia Budi No. 1, Medan Sunggal"),
                Map.of("address","Kantor Dinas PU Medan"),
                Map.of("browser","Chrome 124","os","Android 14","ip","180.244.12.34")));
        records.add(buildAttendanceRecord("Kamis, 24 Apr 2025", "07:58", null, "09:10:00", "ongoing",
                Map.of("address","Kantor Dinas PU Medan"), null,
                Map.of("browser","Chrome 124","os","Android 14","ip","180.244.12.34")));
        model.addAttribute("attendanceRecords", records);
        return "petugas/attendance-history";
    }

    // ==========================================
    // WARGA ROUTES
    // ==========================================

    // GET /warga/login — Ditangani oleh WargaAuthController


    @GetMapping("/warga/module")
    public String wargaModule() { return "warga/module"; }

    @GetMapping("/warga/dashboard")
    public String wargaDashboard(Model model, HttpSession session) {
        // Cek session
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/warga/login";
        }

        // Ambil data user dari database
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            session.invalidate();
            return "redirect:/warga/login";
        }

        User user = userOpt.get();
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("name", user.getFullName());
        userMap.put("email", user.getEmail());
        userMap.put("id", user.getUserId());
        model.addAttribute("user", userMap);

        // ABSTRACTION: hanya laporan milik warga ini (via service)
        List<Report> dbReports = reportService.getReportsByWarga(userId);
        long total = dbReports.size();
        long diproses = dbReports.stream().filter(r -> r.getStatus() == ReportStatus.DITUGASKAN || r.getStatus() == ReportStatus.SEDANG_DIKERJAKAN).count();
        long selesai = dbReports.stream().filter(r -> r.getStatus() == ReportStatus.SELESAI).count();
        long ditolak = dbReports.stream().filter(r -> r.getStatus() == ReportStatus.DITOLAK).count();
        long menunggu = dbReports.stream().filter(r -> r.getStatus() == ReportStatus.MENUNGGU_VALIDASI).count();

        List<Map<String, Object>> stats = new ArrayList<>();
        stats.add(Map.of("icon","file-text",   "color","bg-blue-100 text-blue-600",   "count",total,"label","Total Laporan"));
        stats.add(Map.of("icon","clock",        "color","bg-yellow-100 text-yellow-600","count",menunggu,"label","Menunggu"));
        stats.add(Map.of("icon","tool",         "color","bg-orange-100 text-orange-600","count",diproses,"label","Diproses"));
        stats.add(Map.of("icon","check-circle", "color","bg-green-100 text-green-600", "count",selesai,"label","Selesai"));
        stats.add(Map.of("icon","x-circle",     "color","bg-red-100 text-red-600",     "count",ditolak,"label","Ditolak"));
        model.addAttribute("stats", stats);

        List<Map<String, Object>> recentReports = new ArrayList<>();
        for (Report r : dbReports.stream().limit(5).toList()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getReportId());
            map.put("title", r.getTicketNumber());
            map.put("category", r.getCategory() != null ? r.getCategory().getCategoryName() : "Lainnya");
            map.put("status", toWargaStatusLabel(r.getStatus()));
            String colorClass = switch (r.getStatus()) {
                case MENUNGGU_VALIDASI -> "bg-gray-100 text-gray-700";
                case DIVALIDASI, DITUGASKAN, SEDANG_DIKERJAKAN -> "bg-yellow-100 text-yellow-700";
                case SELESAI -> "bg-green-100 text-green-700";
                case DITOLAK -> "bg-red-100 text-red-700";
                default -> "bg-gray-100 text-gray-700";
            };
            map.put("statusColor", colorClass);
            map.put("location", r.getLocationHint());
            map.put("date", r.getSubmittedAt() != null ? r.getSubmittedAt().toLocalDate() : java.time.LocalDate.now());
            recentReports.add(map);
        }
        model.addAttribute("recentReports", recentReports);
        // Notifikasi unread count (ABSTRACTION via NotificationService)
        model.addAttribute("unreadCount", notificationService.countUnreadByUser(userId));
        return "warga/dashboard";
    }

    @GetMapping("/warga/create-report")
    public String wargaCreateReport(Model model, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) return "redirect:/warga/login";
        model.addAttribute("createReportDTO", new com.plr.aduaja.dto.CreateReportDTO());
        model.addAttribute("categories", reportCategoryRepository.findByIsActiveTrue());
        return "warga/create-report";
    }

    @PostMapping("/warga/create-report")
    public String wargaCreateReportPost(
            @ModelAttribute CreateReportDTO dto,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) return "redirect:/warga/login";

        try {
            Report report = reportService.createReport(dto, userId);
            // Notifikasi ke semua admin pusat
            List<User> admins = userRepository.findByRole(User.Role.ADMIN_PUSAT);
            for (User admin : admins) {
                notificationService.createNotification(
                    admin.getUserId(),
                    "Laporan Baru",
                    "Laporan baru nomor " + report.getTicketNumber() + " telah dibuat dan menunggu validasi.",
                    "REPORT",
                    report.getReportId()
                );
            }
            redirectAttributes.addFlashAttribute("success", "Laporan berhasil dikirim!");
            return "redirect:/warga/report-detail?id=" + report.getReportId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Gagal mengirim laporan: " + e.getMessage());
            return "redirect:/warga/create-report";
        }
    }

    @GetMapping("/warga/report-history")
    public String wargaReportHistory(
            Model model,
            HttpSession session,
            @RequestParam(value = "status", required = false, defaultValue = "Semua") String filterStatus,
            @RequestParam(value = "q",      required = false, defaultValue = "") String searchQuery
    ) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) return "redirect:/warga/login";
        // ABSTRACTION: hanya laporan warga ini
        List<Report> dbReports = reportService.getReportsByWarga(userId);

        List<Map<String, Object>> allReports = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy");
        for (Report r : dbReports) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getReportId());
            map.put("title", r.getTicketNumber());
            map.put("category", r.getCategory() != null ? r.getCategory().getCategoryName() : "Lainnya");
            map.put("status", toWargaStatusLabel(r.getStatus()));
            map.put("location", r.getLocationHint());
            map.put("date", r.getSubmittedAt().toLocalDate());
            map.put("description", r.getDescription());
            map.put("landmark", r.getLocationHint());
            map.put("rejectionReason", "");

            String icon = "file-text";
            String iconColor = "text-gray-600";
            if (r.getCategory() != null && r.getCategory().getCategoryName().contains("Jalan")) { icon = "alert-triangle"; iconColor = "text-orange-600"; }
            else if (r.getCategory() != null && (r.getCategory().getCategoryName().contains("Listrik") || r.getCategory().getCategoryName().contains("Penerangan"))) { icon = "zap"; iconColor = "text-yellow-600"; }
            else if (r.getCategory() != null && r.getCategory().getCategoryName().contains("Drainase")) { icon = "droplets"; iconColor = "text-blue-600"; }
            else if (r.getCategory() != null && r.getCategory().getCategoryName().contains("Taman")) { icon = "tree-pine"; iconColor = "text-green-600"; }

            map.put("icon", icon);
            map.put("iconColor", iconColor);

            String colorClass = switch (r.getStatus()) {
                case MENUNGGU_VALIDASI -> "bg-gray-100 text-gray-700";
                case DIVALIDASI -> "bg-blue-100 text-blue-700";
                case DITUGASKAN, SEDANG_DIKERJAKAN -> "bg-yellow-100 text-yellow-700";
                case SELESAI -> "bg-green-100 text-green-700";
                case DITOLAK -> "bg-red-100 text-red-700";
                case SENGKETA -> "bg-orange-100 text-orange-700";
                default -> "bg-gray-100 text-gray-700";
            };
            map.put("statusColor", colorClass);

            allReports.add(map);
        }

        List<Map<String, Object>> filtered = new ArrayList<>();
        for (Map<String, Object> r : allReports) {
            boolean matchStatus = filterStatus.equals("Semua") || r.get("status").equals(filterStatus);
            boolean matchQuery  = searchQuery.isBlank()
                    || r.get("title").toString().toLowerCase().contains(searchQuery.toLowerCase())
                    || r.get("id").toString().toLowerCase().contains(searchQuery.toLowerCase())
                    || r.get("category").toString().toLowerCase().contains(searchQuery.toLowerCase());
            if (matchStatus && matchQuery) filtered.add(r);
        }

        List<String> statusOptions = List.of("Semua","Menunggu","Diproses","Selesai","Ditolak","Sengketa");
        Map<String, Integer> statusCounts = new HashMap<>();
        statusCounts.put("Semua", allReports.size());
        for (String s : List.of("Menunggu","Diproses","Selesai","Ditolak","Sengketa"))
            statusCounts.put(s, (int) allReports.stream().filter(r -> r.get("status").equals(s)).count());

        model.addAttribute("reports",      filtered);
        model.addAttribute("totalCount",   allReports.size());
        model.addAttribute("filterStatus", filterStatus);
        model.addAttribute("searchQuery",  searchQuery);
        model.addAttribute("statusOptions",statusOptions);
        model.addAttribute("statusCounts", statusCounts);
        return "warga/report-history";
    }

    @GetMapping("/warga/report-detail")
    public String wargaReportDetail(
            Model model,
            HttpSession session,
            @RequestParam(value = "id", required = false) String id
    ) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) return "redirect:/warga/login";

        if (id == null || id.isBlank()) return "redirect:/warga/report-history";

        // ABSTRACTION: cari laporan via service, bukan Repository langsung
        Report report = reportService.findById(id).orElse(null);
        if (report == null) return "redirect:/warga/report-history";

        // Siapkan data untuk view
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");
        Map<String, Object> reportMap = new HashMap<>();
        reportMap.put("id", report.getReportId());
        reportMap.put("reportId", report.getReportId());
        reportMap.put("ticketNumber", report.getTicketNumber());
        reportMap.put("title", report.getTicketNumber());
        reportMap.put("description", report.getDescription());
        reportMap.put("locationHint", report.getLocationHint());
        reportMap.put("landmark", report.getLocationHint());
        reportMap.put("location", report.getLocationHint());
        reportMap.put("latitude", report.getLatitude());
        reportMap.put("longitude", report.getLongitude());
        reportMap.put("photoUrl", report.getPhotoBase64());
        reportMap.put("photoBase64", report.getPhotoBase64());
        reportMap.put("adminNotes", report.getAdminNotes());
        reportMap.put("rejectionReason", report.getRejectionReason());
        reportMap.put("status", toWargaStatusLabel(report.getStatus()));
        reportMap.put("category", report.getCategory() != null ? report.getCategory().getCategoryName() : "Lainnya");
        reportMap.put("submittedAt", report.getSubmittedAt() != null ? report.getSubmittedAt().format(fmt) : "-");
        reportMap.put("createdDate", report.getSubmittedAt() != null ? report.getSubmittedAt().format(fmt) : "-");
        reportMap.put("date", report.getSubmittedAt() != null ? report.getSubmittedAt().toLocalDate() : java.time.LocalDate.now());
        reportMap.put("slaDeadline", "-");
        reportMap.put("revisions", report.getRevisions());
        String cc = switch (report.getStatus()) {
            case MENUNGGU_VALIDASI -> "bg-gray-100 text-gray-700";
            case DIVALIDASI, DITUGASKAN, SEDANG_DIKERJAKAN -> "bg-yellow-100 text-yellow-700";
            case SELESAI -> "bg-green-100 text-green-700";
            case DITOLAK -> "bg-red-100 text-red-700";
            default -> "bg-gray-100 text-gray-700";
        };
        reportMap.put("statusColor", cc);

        model.addAttribute("report", reportMap);
        return "warga/report-detail";
    }




    @GetMapping("/warga/notifications")
    public String wargaNotifications(
            Model model,
            HttpSession session,
            @RequestParam(value = "filter", required = false, defaultValue = "semua") String filter
    ) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) return "redirect:/warga/login";

        // ABSTRACTION: ambil notifikasi dari DB via service
        List<com.plr.aduaja.model.Notification> notifs = filter.equals("belum-dibaca")
                ? notificationService.getUnreadNotificationsByUser(userId)
                : notificationService.getNotificationsByUser(userId);
        long unreadCount = notificationService.countUnreadByUser(userId);

        model.addAttribute("notifications", notifs);
        model.addAttribute("totalCount",  notifs.size());
        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("filter",      filter);
        return "warga/notifications";
    }

    /** POST /warga/notifications/mark-read — tandai semua notifikasi sudah dibaca */
    @PostMapping("/warga/notifications/mark-read")
    public String wargaMarkAllRead(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) return "redirect:/warga/login";
        notificationService.markAllAsReadByUser(userId);
        return "redirect:/warga/notifications";
    }

    // GET /warga/profile — Ditangani oleh WargaAuthController (berbasis session)
    // POST /warga/profile — Ditangani oleh WargaAuthController (via /warga/profile/edit)



    // ==========================================
    // PRIVATE HELPERS — WARGA STATUS LABEL
    // ==========================================

    private String toWargaStatusLabel(Report.ReportStatus status) {
        if (status == null) return "Menunggu";
        return switch (status) {
            case MENUNGGU_VALIDASI -> "Menunggu";
            case PERLU_REVISI -> "Perlu Revisi";
            case DITOLAK -> "Ditolak";
            case DIVALIDASI -> "Divalidasi";
            case DIDISPOSISI -> "Didisposisi";
            case DITUGASKAN -> "Ditugaskan";
            case SEDANG_DIKERJAKAN -> "Diproses";
            case TERTUNDA -> "Tertunda";
            case MENUNGGU_KONFIRMASI -> "Menunggu Konfirmasi";
            case SELESAI -> "Selesai";
            case SENGKETA -> "Sengketa";
            case DITUTUP -> "Ditutup";
        };
    }

    // ==========================================
    // PRIVATE HELPERS — ADMIN DATA CONVERSION
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

    // ==========================================
    // PRIVATE HELPERS — SHARED
    // ==========================================

    private String dummyReportImage() {
        return "data:image/svg+xml;charset=UTF-8,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1200' height='800' viewBox='0 0 1200 800'%3E%3Cdefs%3E%3ClinearGradient id='g' x1='0' x2='1' y1='0' y2='1'%3E%3Cstop offset='0%25' stop-color='%231d4ed8'/%3E%3Cstop offset='100%25' stop-color='%230f766e'/%3E%3C/linearGradient%3E%3C/defs%3E%3Crect width='1200' height='800' fill='url(%23g)'/%3E%3Crect x='70' y='70' width='1060' height='660' rx='36' fill='white' fill-opacity='0.12'/%3E%3Ctext x='600' y='390' text-anchor='middle' fill='white' font-family='Arial, sans-serif' font-size='64' font-weight='700'%3EAduAja%3C/text%3E%3Ctext x='600' y='460' text-anchor='middle' fill='white' font-family='Arial, sans-serif' font-size='28' fill-opacity='0.9'%3EDummy Report Image%3C/text%3E%3C/svg%3E";
    }

    private Map<String, Object> buildAttendanceRecord(
            String dateFormatted, String checkInTime, String checkOutTime,
            String duration, String status,
            Map<String, Object> checkInLoc, Map<String, Object> checkOutLoc,
            Map<String, Object> deviceInfo
    ) {
        Map<String, Object> r = new HashMap<>();
        r.put("dateFormatted",   dateFormatted);
        r.put("checkInTime",     checkInTime);
        r.put("checkOutTime",    checkOutTime);
        r.put("duration",        duration);
        r.put("status",          status);
        r.put("checkInLocation", checkInLoc);
        r.put("checkOutLocation",checkOutLoc);
        r.put("deviceInfo",      deviceInfo);
        return r;
    }

    // ==========================================
    // PRIVATE HELPERS — ENTITY TO VIEW MAP CONVERTERS
    // ==========================================

    private String formatDuration(Duration d) {
        long hours = d.toHours();
        long mins = d.toMinutes() % 60;
        long secs = d.getSeconds() % 60;
        return String.format("%02d:%02d:%02d", hours, mins, secs);
    }

    private Map<String, Object> toPetugasTaskMap(FieldTask task) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", task.getTaskId());
        m.put("title", task.getReport() != null ? task.getReport().getDescription() : "Tugas #" + task.getTaskId().substring(0, 8));
        m.put("category", task.getReport() != null && task.getReport().getCategory() != null
            ? task.getReport().getCategory().getCategoryName() : "Lainnya");
        String status = switch (task.getTaskStatus()) {
            case BARU -> "new";
            case SEDANG_DIKERJAKAN -> "in_progress";
            case TERTUNDA -> "pending";
            case SELESAI -> "completed";
            case DITUGASKAN_ULANG -> "new";
        };
        m.put("status", status);
        m.put("priority", "medium");
        m.put("location", task.getReport() != null ? task.getReport().getLocationHint() : "-");
        m.put("description", task.getReport() != null ? task.getReport().getDescription() : "-");
        m.put("reporterName", task.getReport() != null && task.getReport().getReporter() != null
            ? task.getReport().getReporter().getFullName() : "-");
        m.put("reportDate", task.getReport() != null && task.getReport().getSubmittedAt() != null
            ? task.getReport().getSubmittedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy")) : "-");
        m.put("slaDeadline", "-");
        m.put("slaStatusText", "-");
        m.put("slaStatusClass", "text-gray-600");
        m.put("distanceToTask", "-");
        if (task.getStartedAt() != null) m.put("startedAt", task.getStartedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")));
        m.put("officerLatitude", task.getOfficerLatitude());
        m.put("officerLongitude", task.getOfficerLongitude());
        return m;
    }

    // ==========================================
    // INNER CLASS: Form object untuk create-report
    // ==========================================
    public static class ReportForm {
        private String category;
        private String description;
        private String landmark;
        private String latitude;
        private String longitude;
        private String photoData;

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getLandmark() { return landmark; }
        public void setLandmark(String landmark) { this.landmark = landmark; }
        public String getLatitude() { return latitude; }
        public void setLatitude(String latitude) { this.latitude = latitude; }
        public String getLongitude() { return longitude; }
        public void setLongitude(String longitude) { this.longitude = longitude; }
        public String getPhotoData() { return photoData; }
        public void setPhotoData(String photoData) { this.photoData = photoData; }
    }
}
