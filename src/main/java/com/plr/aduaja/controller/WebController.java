package com.plr.aduaja.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class WebController {

    // ==========================================
    // ROOT REDIRECT
    // ==========================================
    @GetMapping("/")
    public String rootRedirect() {
        return "index";
    }

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

    /** GET /admin/login — halaman login admin */
    @GetMapping("/admin/login")
    public String adminLogin() {
        return "admin/login";
    }

    /** POST /admin/login — proses login admin berdasarkan role */
    @PostMapping("/admin/login")
    public String adminLoginPost(@RequestParam(value = "role", required = false, defaultValue = "admin_pusat") String role) {
        if ("admin_pusat".equalsIgnoreCase(role)) {
            return "redirect:/admin/dashboard";
        } else {
            return "redirect:/admin/dinas/dashboard";
        }
    }

    /** GET /admin/home — redirect ke dashboard */
    @GetMapping("/admin/home")
    public String adminHome() {
        return "redirect:/admin/dashboard";
    }

    /** GET /admin/dashboard — dashboard utama admin (pusat atau dinas) */
    @GetMapping("/admin/dashboard")
    public String adminDashboard(
            Model model,
            @RequestParam(value = "role", required = false, defaultValue = "admin_pusat") String role
    ) {
        model.addAttribute("userRole", role);

        if ("admin_dinas".equalsIgnoreCase(role)) {
            model.addAttribute("dinasName", "Dinas Pekerjaan Umum");
            List<Map<String, Object>> dinasStats = new ArrayList<>();
            dinasStats.add(Map.of("title", "Laporan Diterima", "value", 12,
                    "icon", "inbox", "bgColor", "bg-blue-100", "color", "text-blue-600"));
            dinasStats.add(Map.of("title", "Dalam Penanganan", "value", 5,
                    "icon", "wrench", "bgColor", "bg-yellow-100", "color", "text-yellow-600"));
            dinasStats.add(Map.of("title", "Terlambat SLA", "value", 2,
                    "icon", "alert-circle", "bgColor", "bg-red-100", "color", "text-red-600"));
            dinasStats.add(Map.of("title", "Selesai", "value", 8,
                    "icon", "check-circle", "bgColor", "bg-green-100", "color", "text-green-600"));
            model.addAttribute("stats", dinasStats);
            model.addAttribute("pendingAssignments", dummyIncomingReportsForDinas());
            model.addAttribute("availablePetugas", dummyPetugasList());
        } else {
            // Stat cards for admin_pusat
            List<Map<String, Object>> stats = new ArrayList<>();
            stats.add(Map.of("title", "Laporan Masuk", "value", 24,
                    "icon", "file-text", "bgColor", "bg-blue-100", "color", "text-blue-600"));
            stats.add(Map.of("title", "Menunggu Validasi", "value", 8,
                    "icon", "clock", "bgColor", "bg-yellow-100", "color", "text-yellow-600"));
            stats.add(Map.of("title", "Terlambat SLA", "value", 3,
                    "icon", "alert-triangle", "bgColor", "bg-red-100", "color", "text-red-600"));
            stats.add(Map.of("title", "Selesai Hari Ini", "value", 5,
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
        model.addAttribute("queueReports", dummyQueueReports());

        // Reports for validation tab
        model.addAttribute("validationReports", dummyValidationReports());
        model.addAttribute("selectedValidation", null);

        // Merge tickets
        model.addAttribute("mergeTickets", dummyMergeTickets());
        model.addAttribute("clusters", dummyClusters());
        model.addAttribute("selectedTickets", new ArrayList<>());

        // Disposisi
        model.addAttribute("disposisiReports", dummyDisposisiReports());
        model.addAttribute("dinasList", dummyDinasList());
        model.addAttribute("selectedDisposition", null);

        return "admin/dashboard";
    }

    /** GET /admin/laporan-queue — antrean laporan masuk */
    @GetMapping("/admin/laporan-queue")
    public String adminLaporanQueue(Model model) {
        model.addAttribute("queueReports", dummyQueueReports());
        return "admin/laporan-queue";
    }

    /** GET /admin/validation — panel validasi laporan */
    @GetMapping("/admin/validation")
    public String adminValidationPanel(
            Model model,
            @RequestParam(value = "id", required = false) String id
    ) {
        List<Map<String, Object>> reports = dummyValidationReports();
        model.addAttribute("reports", reports);
        model.addAttribute("pendingCount", reports.size());

        Map<String, Object> selected = null;
        if (id != null) {
            selected = reports.stream()
                    .filter(r -> r.get("id").equals(id))
                    .findFirst().orElse(reports.get(0));
        }
        model.addAttribute("selectedReport", selected);
        return "admin/validation-panel";
    }

    /** POST /admin/validation — proses aksi validasi (approve/reject) */
    @PostMapping("/admin/validation")
    public String adminValidationPost(
            @RequestParam(value = "id", required = false) String id,
            @RequestParam(value = "action", required = false) String action
    ) {
        // Dummy: redirect kembali ke halaman validasi
        return "redirect:/admin/validation" + (id != null ? "?id=" + id : "");
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
        model.addAttribute("mergeTickets", dummyMergeTickets());
        model.addAttribute("clusters", dummyClusters());
        model.addAttribute("selectedTickets", new ArrayList<>());
        return "admin/merge-ticket-panel";
    }

    /** POST /admin/merge — proses merge tiket */
    @PostMapping("/admin/merge")
    public String adminMergeTicketPost() {
        return "redirect:/admin/merge";
    }

    // Alias lama
    @GetMapping("/admin/merge-ticket-panel")
    public String adminMergeTicketPanelAlias(Model model) {
        return adminMergeTicketPanel(model);
    }

    @PostMapping("/admin/merge-ticket-panel")
    public String adminMergeTicketPanelAliasPost() {
        return "redirect:/admin/merge";
    }

    /** GET /admin/disposisi — panel disposisi ke dinas */
    @GetMapping("/admin/disposisi")
    public String adminDisposisiPanel(
            Model model,
            @RequestParam(value = "id", required = false) String id
    ) {
        List<Map<String, Object>> reports = dummyDisposisiReports();
        model.addAttribute("reports", reports);
        model.addAttribute("pendingCount", reports.size());
        model.addAttribute("dinasList", dummyDinasList());

        Map<String, Object> selected = null;
        if (id != null) {
            selected = reports.stream()
                    .filter(r -> r.get("id").equals(id))
                    .findFirst().orElse(reports.get(0));
        }
        model.addAttribute("selectedReport", selected);
        return "admin/disposisi-panel";
    }

    /** POST /admin/disposisi — proses kirim disposisi ke dinas */
    @PostMapping("/admin/disposisi")
    public String adminDisposisiPost(
            @RequestParam(value = "id", required = false) String id,
            @RequestParam(value = "dinasId", required = false) String dinasId,
            @RequestParam(value = "catatan", required = false) String catatan
    ) {
        return "redirect:/admin/disposisi" + (id != null ? "?id=" + id : "");
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
        List<Map<String, Object>> disputes = dummyDisputeList();
        model.addAttribute("disputes", disputes);

        Map<String, Object> selected = null;
        if (id != null) {
            selected = disputes.stream()
                    .filter(d -> d.get("id").equals(id))
                    .findFirst().orElse(disputes.get(0));
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

        List<Map<String, Object>> stats = new ArrayList<>();
        stats.add(Map.of("title", "Laporan Diterima", "value", 12,
                "icon", "inbox", "bgColor", "bg-blue-100", "color", "text-blue-600"));
        stats.add(Map.of("title", "Dalam Penanganan", "value", 5,
                "icon", "wrench", "bgColor", "bg-yellow-100", "color", "text-yellow-600"));
        stats.add(Map.of("title", "Terlambat SLA", "value", 2,
                "icon", "alert-circle", "bgColor", "bg-red-100", "color", "text-red-600"));
        stats.add(Map.of("title", "Selesai", "value", 8,
                "icon", "check-circle", "bgColor", "bg-green-100", "color", "text-green-600"));
        model.addAttribute("stats", stats);

        // Penugasan data
        model.addAttribute("pendingAssignments", dummyIncomingReportsForDinas());
        model.addAttribute("availablePetugas", dummyPetugasList());

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

        List<Map<String, Object>> laporanDinas = dummyDinasQueueReports();
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
        List<Map<String, Object>> incomingReports = dummyIncomingReportsForDinas();
        model.addAttribute("incomingReports", incomingReports);
        model.addAttribute("petugasList", dummyPetugasList());

        Map<String, Object> selected = null;
        if (id != null) {
            selected = incomingReports.stream()
                    .filter(r -> r.get("id").equals(id))
                    .findFirst().orElse(incomingReports.get(0));
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
        List<Map<String, Object>> ticketsInProgress = dummyTicketsInProgress();
        model.addAttribute("ticketsInProgress", ticketsInProgress);

        Map<String, Object> selected = null;
        if (id != null) {
            selected = ticketsInProgress.stream()
                    .filter(t -> t.get("id").equals(id))
                    .findFirst().orElse(ticketsInProgress.get(0));
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
        List<Map<String, Object>> ticketsReady = dummyTicketsReadyToClose();
        model.addAttribute("ticketsReadyToClose", ticketsReady);

        Map<String, Object> selected = null;
        if (id != null) {
            selected = ticketsReady.stream()
                    .filter(t -> t.get("id").equals(id))
                    .findFirst().orElse(ticketsReady.get(0));
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

    // ==========================================
    // PETUGAS ROUTES
    // ==========================================

    private List<Map<String, Object>> currentTasks = new ArrayList<>();

    @GetMapping("/petugas/login")
    public String petugasLogin() {
        return "petugas/login";
    }

    /** POST /petugas/login — proses login petugas */
    @PostMapping("/petugas/login")
    public String petugasLoginPost() {
        currentTasks = allDummyTasks();
        return "redirect:/petugas/dashboard";
    }

    /** GET /petugas/home — redirect ke dashboard */
    @GetMapping("/petugas/home")
    public String petugasHome() {
        return "redirect:/petugas/dashboard";
    }

    /** POST /petugas/task-action — proses aksi pada tugas (Start, Postpone, dll) */
    @PostMapping("/petugas/task-action")
    public String petugasTaskAction(
            @RequestParam(value = "id", required = false) String id,
            @RequestParam(value = "action", required = false, defaultValue = "start") String action,
            @RequestParam(value = "description", required = false) String description
    ) {
        if (id != null && currentTasks != null) {
            for (Map<String, Object> t : currentTasks) {
                if (t.get("id").equals(id)) {
                    if ("start".equals(action)) {
                        t.put("status", "in_progress");
                        t.put("startedAt", "04 Mei 2026 10:00");
                    } else if ("pending".equals(action)) {
                        t.put("status", "pending");
                        t.put("pendingReason", description != null ? description : "Ditunda oleh petugas");
                    } else if ("reportback".equals(action)) {
                        t.put("status", "invalid");
                    } else if ("escalation".equals(action)) {
                        // Dummy escalation
                    }
                    break;
                }
            }
        }
        return "redirect:/petugas/task-detail?id=" + id;
    }

    @GetMapping("/petugas/dashboard")
    public String petugasDashboard(
            Model model,
            @RequestParam(value = "checkIn", required = false) Boolean checkIn
    ) {
        if (currentTasks == null || currentTasks.isEmpty()) {
            currentTasks = allDummyTasks();
        }
        
        boolean isCheckedIn = true; 
        if (checkIn != null && !checkIn) {
            isCheckedIn = false;
        }

        Map<String, Object> user = new HashMap<>();
        user.put("name", "Ahmad Fauzi");
        user.put("dinas", "Dinas Pekerjaan Umum");
        model.addAttribute("user", user);

        Map<String, Object> attendance = new HashMap<>();
        attendance.put("checkedIn", isCheckedIn);
        attendance.put("currentStatus", isCheckedIn ? "Siap Bertugas" : "Belum Check-In");
        attendance.put("checkInTime", isCheckedIn ? "08:05" : "-");
        attendance.put("workDuration", isCheckedIn ? "04:32:10" : "00:00:00");
        attendance.put("location", isCheckedIn ? "Kantor Dinas PU Medan" : "-");
        model.addAttribute("attendance", attendance);

        Map<String, Object> stats = new HashMap<>();
        long selesai = currentTasks.stream().filter(t -> "completed".equals(t.get("status"))).count();
        long inProgress = currentTasks.stream().filter(t -> "in_progress".equals(t.get("status"))).count();
        long newTasks = currentTasks.stream().filter(t -> "new".equals(t.get("status"))).count();
        long pending = currentTasks.stream().filter(t -> "pending".equals(t.get("status"))).count();

        stats.put("selesaiHariIni", selesai);
        stats.put("sedangDikerjakan", inProgress);
        stats.put("tugasBaru", newTasks);
        stats.put("tertunda", pending);
        model.addAttribute("stats", stats);

        Map<String, Object> deviceInfo = new HashMap<>();
        deviceInfo.put("browser", "Chrome 124");
        deviceInfo.put("os", "Android 14");
        model.addAttribute("deviceInfo", deviceInfo);

        List<Map<String, Object>> activeTasks = new ArrayList<>();
        activeTasks.add(buildTask("TGS-001", "Perbaikan Jalan Berlubang Jl. Sudirman", "Jalan",
                "in_progress", "high", "Jl. Sudirman No. 10, Medan Kota",
                "Terdapat beberapa lubang besar di badan jalan dengan kedalaman sekitar 15 cm.",
                "Budi Santoso", "28 Apr 2025",
                "30 Apr 2025 17:00", "Sisa 6 jam", "text-orange-600",
                "1,2 km", null, "28 Apr 2025 09:00"));
        activeTasks.add(buildTask("TGS-002", "Penggantian Lampu Jalan Gang Melati", "Penerangan",
                "new", "medium", "Gang Melati No. 5, Medan Baru",
                "Lampu jalan di sepanjang gang mati total sejak 3 hari lalu.",
                "Sari Dewi", "27 Apr 2025",
                "02 Mei 2025 17:00", "Sisa 2 hari", "text-yellow-600",
                "2,5 km", null, null));
        model.addAttribute("activeTasks", activeTasks);

        return "petugas/dashboard";
    }

    @GetMapping("/petugas/tasks")
    public String petugasTasks(Model model) {
        if (currentTasks == null || currentTasks.isEmpty()) {
            currentTasks = allDummyTasks();
        }

        List<Map<String, Object>> tasksNew        = new ArrayList<>();
        List<Map<String, Object>> tasksInProgress = new ArrayList<>();
        List<Map<String, Object>> tasksPending    = new ArrayList<>();

        for (Map<String, Object> t : currentTasks) {
            switch (t.get("status").toString()) {
                case "new"         -> tasksNew.add(t);
                case "in_progress" -> tasksInProgress.add(t);
                case "pending"     -> tasksPending.add(t);
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
            @RequestParam(value = "id", required = false, defaultValue = "TGS-001") String id
    ) {
        if (currentTasks == null || currentTasks.isEmpty()) {
            currentTasks = allDummyTasks();
        }

        Map<String, Object> task = currentTasks.stream()
                .filter(t -> t.get("id").equals(id))
                .findFirst()
                .orElse(currentTasks.get(0));

        task.put("description",
                "Terdapat beberapa lubang besar di badan jalan dengan kedalaman sekitar 15 cm " +
                        "dan diameter 40 cm. Kondisi ini membahayakan pengendara terutama pada malam hari.");

        task.put("reporterPhone", "+62 812-3456-7890");

        Map<String, Object> locationMap = new HashMap<>();
        locationMap.put("address", task.getOrDefault("location", "Jl. Sudirman No. 10, Medan Kota").toString());
        locationMap.put("latitude",  "3.5952");
        locationMap.put("longitude", "98.6722");
        task.put("location", locationMap);

        task.put("distanceToTask", task.getOrDefault("distanceToTask", "1,2 km"));

        List<Map<String, Object>> statusHistory = new ArrayList<>();
        statusHistory.add(Map.of("status", "Tugas Dibuat",    "time", "28 Apr 2025 08:00", "note", "Tugas diterima dari laporan warga"));
        statusHistory.add(Map.of("status", "Mulai Dikerjakan","time", "28 Apr 2025 09:00", "note", "Petugas tiba di lokasi"));
        task.put("statusHistory", statusHistory);

        model.addAttribute("task", task);
        model.addAttribute("user", Map.of("name", "Ahmad Fauzi"));
        model.addAttribute("attendance", Map.of("checkedIn", true));
        return "petugas/task-detail";
    }

    @GetMapping("/petugas/task-execution")
    public String petugasTaskExecution(
            Model model,
            @RequestParam(value = "id", required = false, defaultValue = "TGS-001") String id
    ) {
        if (currentTasks == null || currentTasks.isEmpty()) {
            currentTasks = allDummyTasks();
        }
        
        Map<String, Object> task = currentTasks.stream()
                .filter(t -> t.get("id").equals(id))
                .findFirst()
                .orElse(currentTasks.get(0));

        Map<String, Object> locationMap = new HashMap<>();
        locationMap.put("address", task.getOrDefault("location", "Jl. Sudirman No. 10, Medan Kota").toString());
        locationMap.put("latitude", "3.5952");
        locationMap.put("longitude", "98.6722");
        task.put("location", locationMap);
        task.put("distanceToTask", task.getOrDefault("distanceToTask", "1,2 km"));

        model.addAttribute("task", task);
        
        // Dummy materials for simulation
        List<Map<String, Object>> materials = new ArrayList<>();
        model.addAttribute("materials", materials);
        return "petugas/task-execution";
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
            if (currentTasks != null) {
                for (Map<String, Object> t : currentTasks) {
                    if (t.get("id").equals(id)) {
                        t.put("status", "completed");
                        break;
                    }
                }
            }
            return "redirect:/petugas/dashboard";
        }
        return "redirect:/petugas/task-execution?id=" + id;
    }

    @GetMapping("/petugas/history")
    public String petugasHistory(Model model) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTasks", 12);
        stats.put("avgDuration", "2j 18m");
        stats.put("totalHours", 28);
        model.addAttribute("stats", stats);

        List<Map<String, Object>> tasks = new ArrayList<>();
        tasks.add(Map.of(
                "id", "TGS-001", "title", "Perbaikan Jalan Berlubang Jl. Sudirman",
                "category", "Jalan", "location", "Jl. Sudirman, Medan Kota",
                "status", "approved", "duration", "2j 15m", "completedAt", "28 Apr 2025",
                "photoBefore", 2, "photoAfter", 3, "materialUsed", 2
        ));
        tasks.add(Map.of(
                "id", "TGS-002", "title", "Penggantian Lampu Jalan Gang Melati",
                "category", "Penerangan", "location", "Gang Melati, Medan Baru",
                "status", "pending_review", "duration", "1j 45m", "completedAt", "26 Apr 2025",
                "photoBefore", 2, "photoAfter", 2, "materialUsed", 1
        ));
        tasks.add(Map.of(
                "id", "TGS-003", "title", "Pembersihan Saluran Drainase Jl. Gatot Subroto",
                "category", "Drainase", "location", "Jl. Gatot Subroto, Medan Petisah",
                "status", "approved", "duration", "3j 10m", "completedAt", "25 Apr 2025",
                "photoBefore", 3, "photoAfter", 3, "materialUsed", 0
        ));
        model.addAttribute("tasks", tasks);
        return "petugas/history";
    }

    @GetMapping("/petugas/reports")
    public String petugasReports(
            Model model,
            @RequestParam(value = "period", required = false, defaultValue = "week") String period
    ) {
        Map<String, Object> user = new HashMap<>();
        user.put("name", "Ahmad Fauzi");
        model.addAttribute("user", user);
        model.addAttribute("selectedPeriod", period);

        int total     = period.equals("year") ? 48 : period.equals("month") ? 18 : 6;
        int completed = period.equals("year") ? 44 : period.equals("month") ? 16 : 5;
        int pending   = total - completed;
        int hours     = period.equals("year") ? 112 : period.equals("month") ? 42 : 14;
        String rate   = (int) Math.round((double) completed / total * 100) + "%";

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTasks",     total);
        stats.put("completedTasks", completed);
        stats.put("pendingTasks",   pending);
        stats.put("totalHours",     hours);
        stats.put("avgDuration",    "2j 18m");
        stats.put("completionRate", rate);

        List<Map<String, Object>> categories = new ArrayList<>();
        categories.add(Map.of("name","Jalan",      "count",2,"percentage",40,"colorClass","bg-blue-500"));
        categories.add(Map.of("name","Penerangan", "count",2,"percentage",33,"colorClass","bg-yellow-500"));
        categories.add(Map.of("name","Drainase",   "count",1,"percentage",17,"colorClass","bg-green-500"));
        categories.add(Map.of("name","Lainnya",    "count",1,"percentage",10,"colorClass","bg-gray-400"));
        stats.put("categories", categories);

        List<Map<String, Object>> progress = new ArrayList<>();
        String progressTitle;
        if (period.equals("week")) {
            progressTitle = "Tugas per Hari (7 Hari Terakhir)";
            String[] labels = {"Sen","Sel","Rab","Kam","Jum","Sab","Min"};
            int[]    vals   = {1, 0, 2, 1, 1, 0, 0};
            for (int i = 0; i < labels.length; i++)
                progress.add(Map.of("label", labels[i], "completed", vals[i], "percent", vals[i] * 40));
        } else if (period.equals("month")) {
            progressTitle = "Tugas per Minggu (30 Hari Terakhir)";
            String[] labels = {"Mg 1","Mg 2","Mg 3","Mg 4"};
            int[]    vals   = {4, 5, 4, 3};
            for (int i = 0; i < labels.length; i++)
                progress.add(Map.of("label", labels[i], "completed", vals[i], "percent", vals[i] * 17));
        } else {
            progressTitle = "Tugas per Bulan (1 Tahun)";
            String[] labels = {"Jan","Feb","Mar","Apr","Mei","Jun","Jul","Agt","Sep","Okt","Nov","Des"};
            int[]    vals   = {3,4,5,4,4,3,4,4,5,4,5,5};
            for (int i = 0; i < labels.length; i++)
                progress.add(Map.of("label", labels[i], "completed", vals[i], "percent", vals[i] * 18));
        }
        stats.put("progress",      progress);
        stats.put("progressTitle", progressTitle);

        model.addAttribute("stats", stats);
        return "petugas/reports";
    }

    @GetMapping("/petugas/attendance-history")
    public String petugasAttendanceHistory(Model model) {
        Map<String, Object> user = new HashMap<>();
        user.put("name", "Ahmad Fauzi");
        model.addAttribute("user", user);

        Map<String, Object> summary = new HashMap<>();
        summary.put("daysPresent", 18);
        summary.put("totalHours",  144);
        summary.put("lateCount",   2);
        model.addAttribute("summary", summary);

        List<Map<String, Object>> records = new ArrayList<>();
        Map<String, Object> d1 = Map.of("browser","Chrome 124","os","Android 14","ip","180.244.12.34");
        records.add(buildAttendanceRecord("Senin, 28 Apr 2025", "08:05", "17:02", "08:57:00", "completed",
                Map.of("address","Jl. Setia Budi No. 1, Medan Sunggal"),
                Map.of("address","Jl. Sudirman No. 10, Medan Kota"), d1));

        Map<String, Object> d2 = Map.of("browser","Chrome 124","os","Android 14","ip","180.244.12.34");
        records.add(buildAttendanceRecord("Jumat, 25 Apr 2025", "08:15", "17:00", "08:45:00", "completed",
                Map.of("address","Jl. Setia Budi No. 1, Medan Sunggal"),
                Map.of("address","Kantor Dinas PU Medan"), d2));

        Map<String, Object> d3 = Map.of("browser","Chrome 124","os","Android 14","ip","180.244.12.34");
        records.add(buildAttendanceRecord("Kamis, 24 Apr 2025", "07:58", null, "09:10:00", "ongoing",
                Map.of("address","Kantor Dinas PU Medan"), null, d3));

        model.addAttribute("attendanceRecords", records);
        return "petugas/attendance-history";
    }

    // ==========================================
    // WARGA ROUTES
    // ==========================================

    @GetMapping("/warga/login")
    public String wargaLogin() { return "warga/login"; }

    /** POST /warga/login — proses login warga */
    @PostMapping("/warga/login")
    public String wargaLoginPost() {
        return "redirect:/warga/dashboard";
    }

    @GetMapping("/warga/module")
    public String wargaModule() { return "warga/module"; }

    @GetMapping("/warga/dashboard")
    public String wargaDashboard(Model model) {
        Map<String, Object> user = new HashMap<>();
        user.put("name", "Budi Santoso");
        model.addAttribute("user", user);

        List<Map<String, Object>> stats = new ArrayList<>();
        stats.add(Map.of("icon","file-text",   "color","bg-blue-100 text-blue-600",   "count",5,"label","Total Laporan"));
        stats.add(Map.of("icon","clock",        "color","bg-yellow-100 text-yellow-600","count",2,"label","Diproses"));
        stats.add(Map.of("icon","check-circle", "color","bg-green-100 text-green-600", "count",2,"label","Selesai"));
        stats.add(Map.of("icon","x-circle",     "color","bg-red-100 text-red-600",     "count",1,"label","Ditolak"));
        model.addAttribute("stats", stats);

        List<Map<String, Object>> recentReports = new ArrayList<>();
        recentReports.add(Map.of(
                "id","RPT-001","title","Jalan Berlubang di Jl. Sudirman",
                "category","Infrastruktur Jalan","status","Diproses",
                "statusColor","bg-yellow-100 text-yellow-700",
                "location","Jl. Sudirman, Medan Kota","date",LocalDate.of(2025,4,20)
        ));
        recentReports.add(Map.of(
                "id","RPT-002","title","Lampu Jalan Mati di Gang Melati",
                "category","Listrik Publik","status","Selesai",
                "statusColor","bg-green-100 text-green-700",
                "location","Gang Melati, Medan Baru","date",LocalDate.of(2025,4,15)
        ));
        recentReports.add(Map.of(
                "id","RPT-003","title","Saluran Drainase Tersumbat",
                "category","Sistem Drainase","status","Menunggu",
                "statusColor","bg-gray-100 text-gray-700",
                "location","Jl. Gatot Subroto, Medan Petisah","date",LocalDate.of(2025,4,28)
        ));
        model.addAttribute("recentReports", recentReports);
        return "warga/dashboard";
    }

    @GetMapping("/warga/create-report")
    public String wargaCreateReport(Model model) {
        model.addAttribute("reportForm", new ReportForm());
        return "warga/create-report";
    }

    /** POST /warga/create-report — kirim laporan baru dari warga */
    @PostMapping("/warga/create-report")
    public String wargaCreateReportPost(
            @ModelAttribute ReportForm reportForm,
            Model model
    ) {
        // Dummy: setelah submit langsung redirect ke history
        return "redirect:/warga/report-history";
    }

    @GetMapping("/warga/report-history")
    public String wargaReportHistory(
            Model model,
            @RequestParam(value = "status", required = false, defaultValue = "Semua") String filterStatus,
            @RequestParam(value = "q",      required = false, defaultValue = "") String searchQuery
    ) {
        List<Map<String, Object>> allReports = allDummyWargaReports();

        List<Map<String, Object>> filtered = new ArrayList<>();
        for (Map<String, Object> r : allReports) {
            boolean matchStatus = filterStatus.equals("Semua") || r.get("status").equals(filterStatus);
            boolean matchQuery  = searchQuery.isBlank()
                    || r.get("title").toString().toLowerCase().contains(searchQuery.toLowerCase())
                    || r.get("id").toString().toLowerCase().contains(searchQuery.toLowerCase())
                    || r.get("category").toString().toLowerCase().contains(searchQuery.toLowerCase());
            if (matchStatus && matchQuery) filtered.add(r);
        }

        List<String> statusOptions = List.of("Semua","Menunggu","Diproses","Selesai","Ditolak");
        Map<String, Integer> statusCounts = new HashMap<>();
        statusCounts.put("Semua", allReports.size());
        for (String s : List.of("Menunggu","Diproses","Selesai","Ditolak"))
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
            @RequestParam(value = "id", required = false) String id
    ) {
        Map<String, Object> report = null;
        for (Map<String, Object> r : allDummyWargaReports()) {
            if (r.get("id").equals(id)) { report = r; break; }
        }
        if (report != null) {
            report.put("photoUrl", null);
            report.put("landmark", "Dekat Indomaret Sudirman");

            List<Map<String, Object>> timeline = new ArrayList<>();
            timeline.add(new HashMap<>(Map.of(
                    "icon","file-text","color","bg-blue-100 text-blue-600",
                    "title","Laporan Diterima","date","20 Apr 2025",
                    "description","Laporan Anda telah berhasil dikirim dan sedang menunggu validasi."
            )));
            timeline.add(new HashMap<>(Map.of(
                    "icon","search","color","bg-yellow-100 text-yellow-600",
                    "title","Validasi Laporan","date","21 Apr 2025",
                    "description","Admin sedang memvalidasi kelengkapan laporan.",
                    "note","Estimasi penyelesaian: 3 hari kerja"
            )));
            timeline.add(new HashMap<>(Map.of(
                    "icon","tool","color","bg-orange-100 text-orange-600",
                    "title","Diproses Dinas","date","22 Apr 2025",
                    "description","Laporan diteruskan ke Dinas Pekerjaan Umum untuk ditangani.",
                    "relatedTicket","TKT-2025-042"
            )));
            model.addAttribute("timeline", timeline);
        }
        model.addAttribute("report", report);
        return "warga/report-detail";
    }

    @GetMapping("/warga/notifications")
    public String wargaNotifications(
            Model model,
            @RequestParam(value = "filter", required = false, defaultValue = "semua") String filter
    ) {
        List<Map<String, Object>> allNotifs = new ArrayList<>();
        allNotifs.add(Map.of(
                "title","Laporan Anda Diproses",
                "message","Laporan 'Jalan Berlubang di Jl. Sudirman' (RPT-001) sedang diproses oleh Dinas PU.",
                "time","2 jam lalu","isRead",false,
                "bgColor","bg-blue-100","icon","tool","iconColor","text-blue-600",
                "action",true,"reportId","RPT-001"
        ));
        allNotifs.add(Map.of(
                "title","Laporan Selesai",
                "message","Laporan 'Lampu Jalan Mati di Gang Melati' (RPT-002) telah selesai ditangani.",
                "time","1 hari lalu","isRead",false,
                "bgColor","bg-green-100","icon","check-circle","iconColor","text-green-600",
                "action",true,"reportId","RPT-002"
        ));
        allNotifs.add(Map.of(
                "title","Laporan Diterima",
                "message","Laporan 'Saluran Drainase Tersumbat' (RPT-003) berhasil dikirim.",
                "time","3 hari lalu","isRead",true,
                "bgColor","bg-gray-100","icon","file-text","iconColor","text-gray-600",
                "action",true,"reportId","RPT-003"
        ));

        long unreadCount = allNotifs.stream().filter(n -> !(boolean) n.get("isRead")).count();
        List<Map<String, Object>> displayed = filter.equals("belum-dibaca")
                ? allNotifs.stream().filter(n -> !(boolean) n.get("isRead")).toList()
                : allNotifs;

        model.addAttribute("notifications", displayed);
        model.addAttribute("totalCount",  allNotifs.size());
        model.addAttribute("unreadCount", (int) unreadCount);
        model.addAttribute("filter",      filter);
        return "warga/notifications";
    }

    @GetMapping("/warga/profile")
    public String wargaProfile(Model model) {
        Map<String, Object> profile = new HashMap<>();
        profile.put("name",      "Budi Santoso");
        profile.put("nik",       "1271052504900001");
        profile.put("email",     "budi.santoso@email.com");
        profile.put("phone",     "081234567890");
        profile.put("address",   "Jl. Setia Budi No. 12");
        profile.put("kelurahan", "Babura");
        profile.put("kecamatan","Medan Baru");
        profile.put("kota",      "Kota Medan");
        profile.put("provinsi",  "Sumatera Utara");
        profile.put("photoUrl",  null);
        model.addAttribute("profile", profile);

        List<Map<String, Object>> stats = new ArrayList<>();
        stats.add(Map.of("value",5,"label","Total Laporan"));
        stats.add(Map.of("value",2,"label","Selesai"));
        stats.add(Map.of("value",2,"label","Diproses"));
        model.addAttribute("stats", stats);
        return "warga/profile";
    }

    /** POST /warga/profile — simpan perubahan profil warga */
    @PostMapping("/warga/profile")
    public String wargaProfilePost() {
        return "redirect:/warga/profile";
    }

    // ==========================================
    // PRIVATE HELPERS — ADMIN PUSAT DUMMY DATA
    // ==========================================

    /** Dummy data: laporan untuk antrean admin pusat */
    private List<Map<String, Object>> dummyQueueReports() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(buildQueueReport("TKT-2025-001", "Jalan Berlubang Jl. Sudirman", "Infrastruktur Jalan",
                "Budi Santoso", "Medan Kota", "28 Apr 2025", "Menunggu Verifikasi", "Kritis", "Sisa 6 jam"));
        list.add(buildQueueReport("TKT-2025-002", "Lampu Jalan Mati Gang Melati", "Penerangan Jalan",
                "Sari Dewi", "Medan Baru", "27 Apr 2025", "Menunggu Verifikasi", "Sedang", "Sisa 2 hari"));
        list.add(buildQueueReport("TKT-2025-003", "Saluran Drainase Tersumbat Jl. Gatot Subroto", "Drainase",
                "Rina Hartati", "Medan Petisah", "26 Apr 2025", "Dalam Peninjauan", "Tinggi", "Sisa 1 hari"));
        list.add(buildQueueReport("TKT-2025-004", "Trotoar Rusak Jl. Imam Bonjol", "Infrastruktur Jalan",
                "Hendra Wijaya", "Medan Polonia", "25 Apr 2025", "Menunggu Verifikasi", "Sedang", "Sisa 3 hari"));
        list.add(buildQueueReport("TKT-2025-005", "Pohon Tumbang Taman Sari", "Taman dan Ruang Publik",
                "Dewi Lestari", "Medan Maimun", "24 Apr 2025", "Terlambat (SLA)", "Kritis", "Terlambat 1 hari"));
        return list;
    }

    private Map<String, Object> buildQueueReport(String id, String judul, String kategori,
                                                 String pelapor, String wilayah, String tanggalMasuk, String status,
                                                 String prioritas, String sisaWaktuSLA) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", id);
        m.put("judul", judul);
        m.put("kategori", kategori);
        m.put("pelapor", pelapor);
        m.put("wilayah", wilayah);
        m.put("tanggalMasuk", tanggalMasuk);
        m.put("status", status);
        m.put("prioritas", prioritas);
        m.put("sisaWaktuSLA", sisaWaktuSLA);
        return m;
    }

    /** Dummy data: laporan yang menunggu validasi */
    private List<Map<String, Object>> dummyValidationReports() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(buildValidationReport("TKT-2025-001", "Jalan Berlubang Jl. Sudirman",
                "Infrastruktur Jalan", "Budi Santoso", "081234567890",
                "Jl. Sudirman No. 10, Medan Kota", "3.5952,98.6722",
                "Dekat Indomaret Sudirman", "28 Apr 2025", "28 Apr 2025 07:30",
                "Terdapat lubang besar sedalam 15 cm, membahayakan pengendara.",
                "/img/laporan1.jpg", "Menunggu Verifikasi", "Kritis"));
        list.add(buildValidationReport("TKT-2025-002", "Lampu Jalan Mati Gang Melati",
                "Penerangan Jalan", "Sari Dewi", "085678901234",
                "Gang Melati No. 5, Medan Baru", "3.5870,98.6712",
                "Depan Warung Bu Tini", "27 Apr 2025", "27 Apr 2025 20:00",
                "3 titik lampu jalan mati sejak seminggu yang lalu.",
                "/img/laporan2.jpg", "Menunggu Verifikasi", "Sedang"));
        list.add(buildValidationReport("TKT-2025-003", "Drainase Tersumbat Jl. Gatot Subroto",
                "Drainase", "Rina Hartati", "087890123456",
                "Jl. Gatot Subroto No. 8, Medan Petisah", "3.5931,98.6695",
                "Samping SPBU Gatot Subroto", "26 Apr 2025", "26 Apr 2025 10:15",
                "Saluran drainase tersumbat menyebabkan genangan saat hujan.",
                "/img/laporan3.jpg", "Dalam Peninjauan", "Tinggi"));
        return list;
    }

    private Map<String, Object> buildValidationReport(
            String id, String judul, String kategori, String pelapor, String kontakPelapor,
            String wilayah, String koordinatStr, String patokan, String tanggalMasuk,
            String waktuKejadian, String deskripsi, String foto, String status, String prioritas) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", id);
        m.put("judul", judul);
        m.put("kategori", kategori);
        m.put("pelapor", pelapor);
        m.put("kontakPelapor", kontakPelapor);
        m.put("wilayah", wilayah);
        m.put("koordinatStr", koordinatStr);
        m.put("patokan", patokan);
        m.put("tanggalMasuk", tanggalMasuk);
        m.put("waktuKejadian", waktuKejadian);
        m.put("deskripsi", deskripsi);
        m.put("foto", foto);
        m.put("status", status);
        m.put("prioritas", prioritas);
        return m;
    }

    /** Dummy data: tiket yang terdeteksi sebagai duplikat */
    private List<Map<String, Object>> dummyMergeTickets() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(buildMergeTicket("TKT-2025-006", "Jalan Berlubang Sudirman (2)",
                "Infrastruktur Jalan", "Ahmad Rifai", "28 Apr 2025",
                "Jl. Sudirman, Medan Kota",
                "Lubang jalan di depan kantor pos Sudirman.", 92));
        list.add(buildMergeTicket("TKT-2025-007", "Lubang Jalan Jl. Sudirman Tengah",
                "Infrastruktur Jalan", "Nina Sari", "27 Apr 2025",
                "Jl. Sudirman, Medan Kota",
                "Ada lubang besar di tengah jalan Sudirman.", 87));
        list.add(buildMergeTicket("TKT-2025-008", "Lampu Jalan Padam Gang Melati",
                "Penerangan Jalan", "Rudi Kurniawan", "27 Apr 2025",
                "Gang Melati, Medan Baru",
                "Lampu jalan di gang melati padam sejak 5 hari.", 85));
        return list;
    }

    private Map<String, Object> buildMergeTicket(String id, String judul, String kategori,
                                                 String pelapor, String tanggalMasuk, String wilayah, String deskripsi, int similarityScore) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", id);
        m.put("judul", judul);
        m.put("kategori", kategori);
        m.put("pelapor", pelapor);
        m.put("tanggalMasuk", tanggalMasuk);
        m.put("wilayah", wilayah);
        m.put("deskripsi", deskripsi);
        m.put("similarityScore", similarityScore);
        m.put("foto", "/img/laporan-default.jpg");
        return m;
    }

    /** Dummy data: cluster duplikat */
    private List<List<Map<String, Object>>> dummyClusters() {
        List<List<Map<String, Object>>> clusters = new ArrayList<>();

        List<Map<String, Object>> c1 = new ArrayList<>();
        c1.add(Map.of("id","TKT-2025-001","kategori","Infrastruktur Jalan","wilayah","Jl. Sudirman, Medan Kota"));
        c1.add(Map.of("id","TKT-2025-006","kategori","Infrastruktur Jalan","wilayah","Jl. Sudirman, Medan Kota"));
        c1.add(Map.of("id","TKT-2025-007","kategori","Infrastruktur Jalan","wilayah","Jl. Sudirman, Medan Kota"));
        clusters.add(c1);

        List<Map<String, Object>> c2 = new ArrayList<>();
        c2.add(Map.of("id","TKT-2025-002","kategori","Penerangan Jalan","wilayah","Gang Melati, Medan Baru"));
        c2.add(Map.of("id","TKT-2025-008","kategori","Penerangan Jalan","wilayah","Gang Melati, Medan Baru"));
        clusters.add(c2);

        return clusters;
    }

    /** Dummy data: laporan tervalidasi yang siap disposisi */
    private List<Map<String, Object>> dummyDisposisiReports() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(buildDisposisiReport("TKT-2025-003", "Saluran Drainase Tersumbat Jl. Gatot Subroto",
                "Drainase", "Rina Hartati", "Jl. Gatot Subroto, Medan Petisah",
                "Tervalidasi", "Tinggi", "Dinas PU dan Penataan Ruang"));
        list.add(buildDisposisiReport("TKT-2025-004", "Trotoar Rusak Jl. Imam Bonjol",
                "Infrastruktur Jalan", "Hendra Wijaya", "Jl. Imam Bonjol, Medan Polonia",
                "Tervalidasi", "Sedang", "Dinas PU dan Penataan Ruang"));
        list.add(buildDisposisiReport("TKT-2025-009", "Pencemaran Sungai Deli",
                "Lingkungan Hidup", "Wahyu Nugroho", "Bantaran Sungai Deli, Medan Barat",
                "Tervalidasi", "Kritis", "Dinas Lingkungan Hidup"));
        return list;
    }

    private Map<String, Object> buildDisposisiReport(String id, String judul, String kategori,
                                                     String pelapor, String wilayah, String status, String prioritasSistem, String dinasRekomendasi) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", id);
        m.put("judul", judul);
        m.put("kategori", kategori);
        m.put("pelapor", pelapor);
        m.put("wilayah", wilayah);
        m.put("status", status);
        m.put("prioritasSistem", prioritasSistem);
        m.put("dinasRekomendasi", dinasRekomendasi);
        m.put("foto", "/img/laporan-default.jpg");
        return m;
    }

    /** Dummy data: daftar dinas tujuan disposisi */
    private List<Map<String, Object>> dummyDinasList() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(Map.of("id","dinas_pu","name","Dinas PU dan Penataan Ruang",
                "kategori", List.of("Infrastruktur Jalan","Drainase","Trotoar","Jembatan")));
        list.add(Map.of("id","dinas_dlh","name","Dinas Lingkungan Hidup",
                "kategori", List.of("Lingkungan Hidup","Pencemaran","Sampah")));
        list.add(Map.of("id","dinas_esdm","name","Dinas ESDM",
                "kategori", List.of("Penerangan Jalan","Listrik Publik")));
        list.add(Map.of("id","dinas_perhubungan","name","Dinas Perhubungan",
                "kategori", List.of("Rambu Lalu Lintas","Halte","Parkir")));
        return list;
    }

    /** Dummy data: sengketa untuk panel resolusi */
    private List<Map<String, Object>> dummyDisputeList() {
        List<Map<String, Object>> list = new ArrayList<>();

        Map<String, Object> d1 = new HashMap<>();
        d1.put("id", "SGK-2025-001");
        d1.put("ticketId", "TKT-2025-010");
        d1.put("judul", "Jalan Tidak Diperbaiki dengan Benar");
        d1.put("statusSengketa", "Menunggu Tinjauan");
        d1.put("prioritas", "Tinggi");
        d1.put("tanggalSengketa", "01 Mei 2025");
        d1.put("pelapor", "Budi Santoso");
        d1.put("tanggalLaporan", "10 Apr 2025");
        d1.put("tanggalSelesai", "28 Apr 2025");
        d1.put("statusSebelum", "Selesai");
        d1.put("alasanSengketa", "Jalan sudah dinyatakan selesai diperbaiki, namun kondisi aspal masih berlubang dan tidak rata. Perbaikan tidak tuntas.");
        d1.put("fotoBuktiSengketa", "/img/bukti-sengketa1.jpg");
        d1.put("keteranganDinas", "Pekerjaan perbaikan jalan telah dilakukan sesuai spesifikasi teknis. Kondisi yang dilihat warga adalah bagian lain yang bukan termasuk scope perbaikan.");
        d1.put("dinas", "Dinas PU dan Penataan Ruang");
        list.add(d1);

        Map<String, Object> d2 = new HashMap<>();
        d2.put("id", "SGK-2025-002");
        d2.put("ticketId", "TKT-2025-011");
        d2.put("judul", "Lampu Jalan Masih Mati Setelah Perbaikan");
        d2.put("statusSengketa", "Menunggu Tinjauan");
        d2.put("prioritas", "Sedang");
        d2.put("tanggalSengketa", "30 Apr 2025");
        d2.put("pelapor", "Sari Dewi");
        d2.put("tanggalLaporan", "15 Apr 2025");
        d2.put("tanggalSelesai", "25 Apr 2025");
        d2.put("statusSebelum", "Selesai");
        d2.put("alasanSengketa", "Status tiket sudah ditandai selesai tetapi lampu jalan di Gang Melati masih belum menyala hingga hari ini.");
        d2.put("fotoBuktiSengketa", "/img/bukti-sengketa2.jpg");
        d2.put("keteranganDinas", "Penggantian lampu telah dilakukan pada 25 April. Kemungkinan terjadi kerusakan pada kabel jaringan yang perlu pengecekan lebih lanjut.");
        d2.put("dinas", "Dinas ESDM");
        list.add(d2);

        return list;
    }

    // ==========================================
    // PRIVATE HELPERS — ADMIN DINAS DUMMY DATA
    // ==========================================

    /** Dummy data: laporan di antrean dinas (sudah didisposisikan) */
    private List<Map<String, Object>> dummyDinasQueueReports() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(buildDinasQueueReport("TKT-2025-003", "Saluran Drainase Tersumbat Jl. Gatot Subroto",
                "Drainase", "Rina Hartati", "Medan Petisah", "26 Apr 2025",
                "Belum Ditindaklanjuti", "Tinggi", "02 Mei 2025"));
        list.add(buildDinasQueueReport("TKT-2025-004", "Trotoar Rusak Jl. Imam Bonjol",
                "Infrastruktur Jalan", "Hendra Wijaya", "Medan Polonia", "25 Apr 2025",
                "Dalam Penanganan", "Sedang", "05 Mei 2025"));
        list.add(buildDinasQueueReport("TKT-2025-012", "Jembatan Rusak Jl. Setia Budi",
                "Jembatan", "Agus Prasetyo", "Medan Sunggal", "24 Apr 2025",
                "Terlambat SLA", "Kritis", "30 Apr 2025"));
        list.add(buildDinasQueueReport("TKT-2025-013", "Saluran Air Mampet Jl. Diponegoro",
                "Drainase", "Fitri Handayani", "Medan Maimun", "23 Apr 2025",
                "Belum Ditindaklanjuti", "Rendah", "07 Mei 2025"));
        return list;
    }

    private Map<String, Object> buildDinasQueueReport(String id, String judul, String kategori,
                                                      String pelapor, String wilayah, String tanggalDisposisi, String status,
                                                      String prioritas, String sisaWaktu) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", id);
        m.put("judul", judul);
        m.put("kategori", kategori);
        m.put("pelapor", pelapor);
        m.put("wilayah", wilayah);
        m.put("tanggalDisposisi", tanggalDisposisi);
        m.put("status", status);
        m.put("prioritas", prioritas);
        m.put("sisaWaktu", sisaWaktu);
        return m;
    }

    /** Dummy data: laporan yang masuk ke dinas untuk penugasan petugas */
    private List<Map<String, Object>> dummyIncomingReportsForDinas() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(buildIncomingDinasReport("TKT-2025-003",
                "Saluran Drainase Tersumbat Jl. Gatot Subroto",
                "Drainase", "Tinggi", "26 Apr 2025",
                "Jl. Gatot Subroto, Medan Petisah", "02 Mei 2025 17:00",
                "Segera tangani sebelum musim hujan. Prioritaskan pembersihan di titik utama.",
                "/img/laporan3.jpg"));
        list.add(buildIncomingDinasReport("TKT-2025-004",
                "Trotoar Rusak Jl. Imam Bonjol",
                "Infrastruktur Jalan", "Sedang", "25 Apr 2025",
                "Jl. Imam Bonjol, Medan Polonia", "05 Mei 2025 17:00",
                "Perbaiki area trotoar yang retak dan membahayakan pejalan kaki.",
                "/img/laporan4.jpg"));
        list.add(buildIncomingDinasReport("TKT-2025-012",
                "Jembatan Rusak Jl. Setia Budi",
                "Jembatan", "Kritis", "24 Apr 2025",
                "Jl. Setia Budi, Medan Sunggal", "30 Apr 2025 17:00",
                "URGENT: Jembatan dalam kondisi kritis, butuh penanganan segera.",
                "/img/laporan5.jpg"));
        return list;
    }

    private Map<String, Object> buildIncomingDinasReport(String id, String judul, String kategori,
                                                         String prioritas, String tanggalDisposisi, String wilayah, String deadline,
                                                         String instruksiAdmin, String foto) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", id);
        m.put("judul", judul);
        m.put("kategori", kategori);
        m.put("prioritas", prioritas);
        m.put("tanggalDisposisi", tanggalDisposisi);
        m.put("wilayah", wilayah);
        m.put("deadline", deadline);
        m.put("instruksiAdmin", instruksiAdmin);
        m.put("foto", foto);
        return m;
    }

    /** Dummy data: daftar petugas lapangan */
    private List<Map<String, Object>> dummyPetugasList() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(buildPetugas("PTG-001", "Ahmad Fauzi", "198804152015031002",
                "Tersedia", "Medan Petisah, Medan Polonia", 1, "081234567890"));
        list.add(buildPetugas("PTG-002", "Rizal Harahap", "199002201019011003",
                "Tersedia", "Medan Baru, Medan Maimun", 2, "082345678901"));
        list.add(buildPetugas("PTG-003", "Dedi Susanto", "198506302014041001",
                "Sedang Bertugas", "Medan Sunggal, Medan Barat", 3, "083456789012"));
        return list;
    }

    private Map<String, Object> buildPetugas(String id, String nama, String nip,
                                             String statusKetersediaan, String wilayahTugas, int tugasAktif, String kontak) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", id);
        m.put("nama", nama);
        m.put("nip", nip);
        m.put("statusKetersediaan", statusKetersediaan);
        m.put("wilayahTugas", wilayahTugas);
        m.put("tugasAktif", tugasAktif);
        m.put("kontak", kontak);
        return m;
    }

    /** Dummy data: tiket yang sedang dalam penanganan (untuk progress update) */
    private List<Map<String, Object>> dummyTicketsInProgress() {
        List<Map<String, Object>> list = new ArrayList<>();

        Map<String, Object> t1 = new HashMap<>();
        t1.put("id", "TKT-2025-004");
        t1.put("judul", "Trotoar Rusak Jl. Imam Bonjol");
        t1.put("kategori", "Infrastruktur Jalan");
        t1.put("prioritas", "Sedang");
        t1.put("pelapor", "Hendra Wijaya");
        t1.put("deadline", "05 Mei 2025 17:00");
        t1.put("foto", "/img/laporan4.jpg");

        List<Map<String, Object>> ph1 = new ArrayList<>();
        ph1.add(Map.of("tanggal","29 Apr 2025 09:00","petugas","Ahmad Fauzi",
                "keterangan","Petugas tiba di lokasi, melakukan survei awal. Menemukan 3 titik kerusakan sepanjang 12 meter.",
                "estimasi","03 Mei 2025"));
        ph1.add(Map.of("tanggal","30 Apr 2025 10:30","petugas","Ahmad Fauzi",
                "keterangan","Pengerjaan dimulai pada titik pertama. Material telah tersedia di lokasi.",
                "estimasi","03 Mei 2025"));
        t1.put("progressHistory", ph1);
        list.add(t1);

        Map<String, Object> t2 = new HashMap<>();
        t2.put("id", "TKT-2025-003");
        t2.put("judul", "Saluran Drainase Tersumbat Jl. Gatot Subroto");
        t2.put("kategori", "Drainase");
        t2.put("prioritas", "Tinggi");
        t2.put("pelapor", "Rina Hartati");
        t2.put("deadline", "02 Mei 2025 17:00");
        t2.put("foto", "/img/laporan3.jpg");

        List<Map<String, Object>> ph2 = new ArrayList<>();
        ph2.add(Map.of("tanggal","28 Apr 2025 14:00","petugas","Rizal Harahap",
                "keterangan","Pembersihan saluran dimulai. Ditemukan sumbatan lumpur dan sampah padat.",
                "estimasi","01 Mei 2025"));
        t2.put("progressHistory", ph2);
        list.add(t2);

        return list;
    }

    /** Dummy data: tiket yang siap ditutup */
    private List<Map<String, Object>> dummyTicketsReadyToClose() {
        List<Map<String, Object>> list = new ArrayList<>();

        Map<String, Object> t1 = new HashMap<>();
        t1.put("id", "TKT-2025-014");
        t1.put("judul", "Perbaikan Jalan Jl. Sutomo");
        t1.put("kategori", "Infrastruktur Jalan");
        t1.put("prioritas", "Tinggi");
        t1.put("pelapor", "Sigit Wibowo");
        t1.put("wilayah", "Jl. Sutomo, Medan Timur");
        t1.put("foto", "/img/laporan6.jpg");

        List<Map<String, Object>> ph1 = new ArrayList<>();
        ph1.add(Map.of("tanggal","25 Apr 2025","keterangan","Survei lokasi dan persiapan material."));
        ph1.add(Map.of("tanggal","27 Apr 2025","keterangan","Pengerjaan patching aspal selesai di seluruh titik."));
        ph1.add(Map.of("tanggal","29 Apr 2025","keterangan","Quality check selesai, kondisi jalan sudah layak."));
        t1.put("progressHistory", ph1);
        list.add(t1);

        Map<String, Object> t2 = new HashMap<>();
        t2.put("id", "TKT-2025-015");
        t2.put("judul", "Penggantian Tutup Gorong-gorong Jl. Asia");
        t2.put("kategori", "Drainase");
        t2.put("prioritas", "Sedang");
        t2.put("pelapor", "Lestari Wulandari");
        t2.put("wilayah", "Jl. Asia, Medan Kota");
        t2.put("foto", "/img/laporan7.jpg");

        List<Map<String, Object>> ph2 = new ArrayList<>();
        ph2.add(Map.of("tanggal","26 Apr 2025","keterangan","Material tutup gorong-gorong tiba."));
        ph2.add(Map.of("tanggal","28 Apr 2025","keterangan","Pemasangan tutup gorong-gorong selesai."));
        t2.put("progressHistory", ph2);
        list.add(t2);

        return list;
    }

    // ==========================================
    // PRIVATE HELPERS — PETUGAS DUMMY DATA
    // ==========================================

    private Map<String, Object> buildTask(
            String id, String title, String category,
            String status, String priority, String location, String description,
            String reporterName, String reportDate,
            String slaDeadline, String slaStatusText, String slaStatusClass,
            String distanceToTask, String pendingReason, String startedAt
    ) {
        Map<String, Object> t = new HashMap<>();
        t.put("id",            id);
        t.put("title",         title);
        t.put("category",      category);
        t.put("status",        status);
        t.put("priority",      priority);
        t.put("location",      location);
        t.put("description",   description);
        t.put("reporterName",  reporterName);
        t.put("reportDate",    reportDate);
        t.put("slaDeadline",   slaDeadline);
        t.put("slaStatusText", slaStatusText);
        t.put("slaStatusClass", slaStatusClass);
        t.put("distanceToTask", distanceToTask);
        if (pendingReason != null) t.put("pendingReason", pendingReason);
        if (startedAt     != null) t.put("startedAt",     startedAt);
        return t;
    }

    private List<Map<String, Object>> allDummyTasks() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(buildTask("TGS-001",
                "Perbaikan Jalan Berlubang Jl. Sudirman",
                "Jalan",
                "new", "critical",
                "Jl. Sudirman No. 10, Medan Kota",
                "Terdapat beberapa lubang besar di badan jalan dengan kedalaman sekitar 15 cm dan diameter 40 cm. Kondisi ini membahayakan pengendara terutama pada malam hari.",
                "Budi Santoso", "28 Apr 2025",
                "30 Apr 2025 17:00", "Sisa 6 jam", "text-red-600",
                "1,2 km", null, null));
        list.add(buildTask("TGS-002",
                "Penggantian Lampu Jalan Gang Melati",
                "Penerangan",
                "in_progress", "medium",
                "Gang Melati No. 5, Medan Baru",
                "Lampu jalan di sepanjang gang mati total sejak 3 hari lalu. Warga meminta segera diganti agar tidak terjadi kecelakaan.",
                "Sari Dewi", "27 Apr 2025",
                "02 Mei 2025 17:00", "Sisa 2 hari", "text-yellow-600",
                "2,5 km", null, "28 Apr 2025 10:15"));
        list.add(buildTask("TGS-003",
                "Pembersihan Saluran Drainase Jl. Gatot Subroto",
                "Drainase",
                "new", "high",
                "Jl. Gatot Subroto No. 8, Medan Petisah",
                "Saluran drainase tersumbat sampah dan lumpur sepanjang 50 meter. Menyebabkan genangan air saat hujan deras.",
                "Rina Hartati", "26 Apr 2025",
                "01 Mei 2025 17:00", "Sisa 1 hari", "text-orange-600",
                "3,8 km", null, null));
        list.add(buildTask("TGS-004",
                "Pemasangan Trotoar Rusak Jl. Imam Bonjol",
                "Trotoar",
                "pending", "medium",
                "Jl. Imam Bonjol, Medan Polonia",
                "Trotoar sepanjang 20 meter mengalami retak dan amblas. Pejalan kaki terpaksa berjalan di badan jalan.",
                "Hendra Wijaya", "25 Apr 2025",
                "05 Mei 2025 17:00", "Sisa 5 hari", "text-green-600",
                "4,1 km", "Menunggu material aspal dari gudang. Estimasi tiba 2 Mei 2025.", null));
        list.add(buildTask("TGS-005",
                "Perbaikan Taman Bermain Taman Sari",
                "Taman",
                "new", "low",
                "Taman Sari, Medan Maimun",
                "Beberapa fasilitas bermain anak rusak berat dan berkarat. Ayunan putus dan perosotan retak.",
                "Dewi Lestari", "24 Apr 2025",
                "07 Mei 2025 17:00", "Sisa 7 hari", "text-green-600",
                "5,0 km", null, null));
        list.add(buildTask("TGS-006",
                "Pemasangan Rambu Lalu Lintas Jl. Sisingamangaraja",
                "Rambu Jalan",
                "in_progress", "high",
                "Jl. Sisingamangaraja No. 15, Medan Kota",
                "Rambu lalu lintas di persimpangan roboh akibat angin kencang. Perlu segera dipasang ulang untuk keamanan.",
                "Ahmad Fauzi", "28 Apr 2025",
                "29 Apr 2025 17:00", "Sisa 4 jam", "text-red-600",
                "0,8 km", null, "28 Apr 2025 11:30"));
        list.add(buildTask("TGS-007",
                "Perbaikan Jembatan Kayu Kampung Sei Sikambing",
                "Jembatan",
                "pending", "critical",
                "Kampung Sei Sikambing, Medan Selatan",
                "Jembatan kayu penghubung antar kampung mulai lapuk dan beberapa papan sudah bolong. Sangat berbahaya bagi warga.",
                "M. Rizki", "23 Apr 2025",
                "26 Apr 2025 17:00", "Sisa 1 hari", "text-orange-600",
                "6,2 km", "Menunggu persetujuan anggaran dari dinas terkait.", null));
        return list;
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
    // PRIVATE HELPERS — WARGA DUMMY DATA
    // ==========================================

    private List<Map<String, Object>> allDummyWargaReports() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(new HashMap<>(Map.of(
                "id","RPT-001","title","Jalan Berlubang di Jl. Sudirman",
                "category","Infrastruktur Jalan","status","Diproses",
                "statusColor","bg-yellow-100 text-yellow-700",
                "location","Jl. Sudirman, Medan Kota","date",LocalDate.of(2025,4,20),
                "icon","alert-triangle","iconColor","text-yellow-600"
        )));
        list.add(new HashMap<>(Map.of(
                "id","RPT-002","title","Lampu Jalan Mati di Gang Melati",
                "category","Listrik Publik","status","Selesai",
                "statusColor","bg-green-100 text-green-700",
                "location","Gang Melati, Medan Baru","date",LocalDate.of(2025,4,15),
                "icon","zap","iconColor","text-green-600"
        )));
        list.add(new HashMap<>(Map.of(
                "id","RPT-003","title","Saluran Drainase Tersumbat",
                "category","Sistem Drainase","status","Menunggu",
                "statusColor","bg-gray-100 text-gray-700",
                "location","Jl. Gatot Subroto, Medan Petisah","date",LocalDate.of(2025,4,28),
                "icon","droplets","iconColor","text-blue-600"
        )));
        list.add(new HashMap<>(Map.of(
                "id","RPT-004","title","Taman Bermain Rusak di Taman Sari",
                "category","Taman dan Ruang Publik","status","Ditolak",
                "statusColor","bg-red-100 text-red-700",
                "location","Taman Sari, Medan Maimun","date",LocalDate.of(2025,3,10),
                "icon","tree-pine","iconColor","text-red-600"
        )));
        list.add(new HashMap<>(Map.of(
                "id","RPT-005","title","Trotoar Retak Jl. Imam Bonjol",
                "category","Infrastruktur Jalan","status","Selesai",
                "statusColor","bg-green-100 text-green-700",
                "location","Jl. Imam Bonjol, Medan Polonia","date",LocalDate.of(2025,3,5),
                "icon","alert-triangle","iconColor","text-green-600"
        )));
        return list;
    }

    // ==========================================
    // INNER CLASS: Form object untuk create-report
    // ==========================================
    public static class ReportForm {
        private String description;
        private String landmark;
        private String latitude;
        private String longitude;

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getLandmark() { return landmark; }
        public void setLandmark(String landmark) { this.landmark = landmark; }
        public String getLatitude() { return latitude; }
        public void setLatitude(String latitude) { this.latitude = latitude; }
        public String getLongitude() { return longitude; }
        public void setLongitude(String longitude) { this.longitude = longitude; }
    }
}