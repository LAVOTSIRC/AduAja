package com.plr.aduaja.controller;

import lombok.extern.slf4j.Slf4j;
import com.plr.aduaja.model.*;
import com.plr.aduaja.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Controller
public class AdminDinasController {

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
    private AgencyService agencyService;

    @GetMapping("/admin/dinas/dashboard")
    public String adminDinasDashboard(Model model) {
        model.addAttribute("dinasName", "Dinas Pekerjaan Umum");
        long diterima = reportService.countByStatus(Report.ReportStatus.DIDISPOSISI);
        long diproses = fieldTaskService.countByStatus(FieldTask.TaskStatus.SEDANG_DIKERJAKAN);
        long selesai = fieldTaskService.countByStatus(FieldTask.TaskStatus.SELESAI);
        long baru = fieldTaskService.countByStatus(FieldTask.TaskStatus.BARU);
        List<Map<String, Object>> stats = new ArrayList<>();
        stats.add(Map.of("title", "Laporan Diterima", "value", diterima, "icon", "inbox", "bgColor", "bg-blue-100", "color", "text-blue-600"));
        stats.add(Map.of("title", "Tugas Baru", "value", baru, "icon", "inbox", "bgColor", "bg-indigo-100", "color", "text-indigo-600"));
        stats.add(Map.of("title", "Dalam Penanganan", "value", diproses, "icon", "wrench", "bgColor", "bg-yellow-100", "color", "text-yellow-600"));
        stats.add(Map.of("title", "Selesai", "value", selesai, "icon", "check-circle", "bgColor", "bg-green-100", "color", "text-green-600"));
        model.addAttribute("stats", stats);

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

    @GetMapping("/admin/dinas/dinas-dashboard")
    public String adminDinasDashboardAlias(Model model) {
        return adminDinasDashboard(model);
    }

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
                m.put("kategori", d.getReport() != null && d.getReport().getCategory() != null ? d.getReport().getCategory().getCategoryName() : "Lainnya");
                m.put("pelapor", d.getReport() != null && d.getReport().getReporter() != null ? d.getReport().getReporter().getFullName() : "-");
                m.put("wilayah", d.getReport() != null && d.getReport().getLocationHint() != null ? d.getReport().getLocationHint() : "-");
                m.put("tanggalDisposisi", d.getDispatchedAt() != null ? d.getDispatchedAt().format(fmt) : "-");
                m.put("status", "Belum Ditindaklanjuti");
                m.put("prioritas", "Sedang");
                m.put("sisaWaktu", "-");
                laporanDinas.add(m);
            }
        }

        long terlambatCount = laporanDinas.stream().filter(r -> "Terlambat SLA".equals(r.get("status"))).count();
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

    @GetMapping("/admin/dinas/dinas-queue")
    public String adminDinasQueueAlias(Model model,
                                       @RequestParam(value = "page", required = false, defaultValue = "1") int page) {
        return adminDinasQueue(model, page);
    }

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
            if (selected == null && !incomingReports.isEmpty()) selected = incomingReports.get(0);
        } else if (!incomingReports.isEmpty()) {
            selected = incomingReports.get(0);
        }
        model.addAttribute("selectedReport", selected);
        return "admin/dinas/penugasan-petugas";
    }

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
        } catch (Exception e) {
            log.error("Gagal penugasan petugas: {}", e.getMessage(), e);
        }
        return "redirect:/admin/dinas/penugasan" + (id != null ? "?id=" + id : "");
    }

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
                m.put("kategori", t.getReport() != null && t.getReport().getCategory() != null ? t.getReport().getCategory().getCategoryName() : "Lainnya");
                m.put("prioritas", "Sedang");
                m.put("pelapor", t.getReport() != null && t.getReport().getReporter() != null ? t.getReport().getReporter().getFullName() : "-");
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

    @PostMapping("/admin/dinas/progress")
    public String adminDinasProgressPost(
            @RequestParam(value = "id", required = false) String id,
            @RequestParam(value = "keterangan", required = false) String keterangan,
            @RequestParam(value = "estimasi", required = false) String estimasi
    ) {
        try {
            fieldTaskService.startTask(id, null, null);
        } catch (Exception e) {
            log.error("Gagal update progress: {}", e.getMessage(), e);
        }
        return "redirect:/admin/dinas/progress" + (id != null ? "?id=" + id : "");
    }

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
                m.put("kategori", t.getReport() != null && t.getReport().getCategory() != null ? t.getReport().getCategory().getCategoryName() : "Lainnya");
                m.put("prioritas", "Sedang");
                m.put("pelapor", t.getReport() != null && t.getReport().getReporter() != null ? t.getReport().getReporter().getFullName() : "-");
                m.put("wilayah", t.getReport() != null && t.getReport().getLocationHint() != null ? t.getReport().getLocationHint() : "-");
                m.put("foto", dummyReportImage());
                List<Map<String, Object>> ph = new ArrayList<>();
                if (t.getStartedAt() != null) ph.add(Map.of("tanggal", t.getStartedAt().format(fmt), "keterangan", "Pengerjaan dimulai"));
                if (t.getCompletedAt() != null) ph.add(Map.of("tanggal", t.getCompletedAt().format(fmt), "keterangan", "Pengerjaan selesai"));
                m.put("progressHistory", ph);
                ticketsReady.add(m);
            }
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

    @PostMapping("/admin/dinas/close")
    public String adminDinasClosePost(
            @RequestParam(value = "id", required = false) String id,
            @RequestParam(value = "keterangan", required = false) String keterangan
    ) {
        try {
            fieldTaskService.completeTask(id);
        } catch (Exception e) {
            log.error("Gagal close tiket {}: {}", id, e.getMessage(), e);
        }
        return "redirect:/admin/dinas/close" + (id != null ? "?id=" + id : "");
    }

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
            if (selected == null && !disputes.isEmpty()) selected = disputes.get(0);
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

    @PostMapping("/admin/dinas/sengketa")
    public String adminDinasSengketaPost(
            @RequestParam(value = "id", required = false) String id,
            @RequestParam(value = "keputusan", required = false) String keputusan,
            @RequestParam(value = "catatan", required = false) String catatan,
            @RequestParam(value = "petugasId", required = false) String petugasId,
            HttpSession session
    ) {
        String adminId = (String) session.getAttribute("userId");
        if (adminId == null) {
            adminId = userService.getUserByEmail("admin.pu@aduaja.go.id")
                    .map(User::getUserId).orElse(null);
        }

        try {
            if ("diterima".equals(keputusan) && petugasId != null && !petugasId.isEmpty()) {
                if (adminId != null) {
                    disputeService.resolveDispute(id, DisputeRecord.ResolutionType.TUGASKAN_KEMBALI, adminId, catatan);
                }
                try {
                    DisputeRecord dispute = disputeService.getDisputeById(id).orElse(null);
                    if (dispute != null && dispute.getReport() != null) {
                        String reportId = dispute.getReport().getReportId();
                        List<FieldTask> relatedTasks = fieldTaskService.getTasksByReport(reportId);
                        for (FieldTask task : relatedTasks) {
                            if (task.getTaskStatus() != FieldTask.TaskStatus.SELESAI) {
                                fieldTaskService.reassignTask(task.getTaskId(), petugasId);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("Gagal reassign petugas saat sengketa: {}", e.getMessage(), e);
                }
                return "redirect:/admin/dinas/sengketa?reassigned=true";
            } else {
                if (adminId != null) {
                    disputeService.resolveDispute(id, DisputeRecord.ResolutionType.TUTUP_LAPORAN, adminId, catatan);
                }
            }
        } catch (Exception e) {
            log.error("Gagal proses sengketa {}: {}", id, e.getMessage(), e);
        }

        return "redirect:/admin/dinas/sengketa" + (id != null ? "?id=" + id : "");
    }

    private String dummyReportImage() {
        return "data:image/svg+xml;charset=UTF-8,%3Csvg xmlns='http://www.w3.org/2000/svg' width='1200' height='800' viewBox='0 0 1200 800'%3E%3Cdefs%3E%3ClinearGradient id='g' x1='0' x2='1' y1='0' y2='1'%3E%3Cstop offset='0%25' stop-color='%231d4ed8'/%3E%3Cstop offset='100%25' stop-color='%230f766e'/%3E%3C/linearGradient%3E%3C/defs%3E%3Crect width='1200' height='800' fill='url(%23g)'/%3E%3Crect x='70' y='70' width='1060' height='660' rx='36' fill='white' fill-opacity='0.12'/%3E%3Ctext x='600' y='390' text-anchor='middle' fill='white' font-family='Arial, sans-serif' font-size='64' font-weight='700'%3EAduAja%3C/text%3E%3Ctext x='600' y='460' text-anchor='middle' fill='white' font-family='Arial, sans-serif' font-size='28' fill-opacity='0.9'%3EDummy Report Image%3C/text%3E%3C/svg%3E";
    }
}
