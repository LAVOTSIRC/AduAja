package com.plr.aduaja.controller;

import lombok.extern.slf4j.Slf4j;
import com.plr.aduaja.dto.CreatePetugasDTO;
import com.plr.aduaja.model.*;
import com.plr.aduaja.repository.RegionRepository;
import com.plr.aduaja.repository.UserProfileRepository;
import com.plr.aduaja.repository.UserRepository;
import com.plr.aduaja.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
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
    private UserRepository userRepository;

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

    @Autowired
    private SlaRecordService slaRecordService;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private RegionRepository regionRepository;

    @GetMapping("/admin/dinas/dashboard")
    public String adminDinasDashboard(Model model, HttpSession session) {
        // SESSION CHECK — semua halaman admin harus login
        String sessionUserId = ControllerHelper.requireAgencySession(session);
        if (sessionUserId == null) return "redirect:/admin/login";

        String agencyId = ControllerHelper.getSessionAgencyId(session);
        String agencyName = ControllerHelper.getSessionAgencyName(session);

        model.addAttribute("dinasName", agencyName != null ? agencyName : "Dinas Pekerjaan Umum");
        
        List<Disposition> allDisp;
        if (agencyId != null) {
            allDisp = dispositionService.getDispositionsByAgency(agencyId);
        } else {
            allDisp = dispositionService.getAllDispositions();
        }

        long diterima = allDisp.size();
        long diproses = allDisp.stream()
                .filter(d -> d.getReport() != null)
                .flatMap(d -> fieldTaskService.getTasksByReport(d.getReport().getReportId()).stream())
                .filter(t -> t.getTaskStatus() == FieldTask.TaskStatus.SEDANG_DIKERJAKAN)
                .count();
        long selesai = allDisp.stream()
                .filter(d -> d.getReport() != null)
                .flatMap(d -> fieldTaskService.getTasksByReport(d.getReport().getReportId()).stream())
                .filter(t -> t.getTaskStatus() == FieldTask.TaskStatus.SELESAI)
                .count();
        long baru = allDisp.stream()
                .filter(d -> d.getReport() != null)
                .flatMap(d -> fieldTaskService.getTasksByReport(d.getReport().getReportId()).stream())
                .filter(t -> t.getTaskStatus() == FieldTask.TaskStatus.BARU)
                .count();
                
        List<Map<String, Object>> stats = new ArrayList<>();
        stats.add(Map.of("title", "Laporan Diterima", "value", diterima, "icon", "inbox", "bgColor", "bg-blue-100", "color", "text-blue-600"));
        stats.add(Map.of("title", "Tugas Baru", "value", baru, "icon", "inbox", "bgColor", "bg-indigo-100", "color", "text-indigo-600"));
        stats.add(Map.of("title", "Dalam Penanganan", "value", diproses, "icon", "wrench", "bgColor", "bg-yellow-100", "color", "text-yellow-600"));
        stats.add(Map.of("title", "Selesai", "value", selesai, "icon", "check-circle", "bgColor", "bg-green-100", "color", "text-green-600"));
        model.addAttribute("stats", stats);

        List<Map<String, Object>> pendingAssignments = new ArrayList<>();
        for (Disposition d : allDisp) {
            if (d.getReport() != null) {
                List<FieldTask> existingTasks = fieldTaskService.getTasksByReport(d.getReport().getReportId());
                if (existingTasks.isEmpty()) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", d.getReport().getReportId());
                    m.put("judul", d.getReport().getTicketNumber() != null ? d.getReport().getTicketNumber() : "Laporan");
                    m.put("kategori", d.getReport().getCategory() != null ? d.getReport().getCategory().getCategoryName() : "Lainnya");
                    m.put("prioritas", d.getPriority() != null ? d.getPriority() : "Sedang");
                    m.put("slaStatus", "-");
                    pendingAssignments.add(m);
                }
            }
        }
        model.addAttribute("pendingAssignments", pendingAssignments);

        List<Map<String, Object>> petugasList = buildPetugasList(agencyId);
        model.addAttribute("availablePetugas", petugasList.isEmpty() ? new ArrayList<>() : petugasList);

        return "admin/dinas/dinas-dashboard";
    }

    @GetMapping("/admin/dinas/dinas-dashboard")
    public String adminDinasDashboardAlias(Model model, HttpSession session) {
        return adminDinasDashboard(model, session);
    }

    private String computeOfficerStatus(String officerId) {
        Optional<OfficerAttendance> shift = attendanceService.getCurrentShift(officerId);
        if (shift.isEmpty()) return "Selesai Shift";
        OfficerAttendance.ShiftStatus shiftStatus = shift.get().getShiftStatus();
        if (shiftStatus == OfficerAttendance.ShiftStatus.SELESAI_SHIFT) return "Selesai Shift";
        if (shiftStatus == OfficerAttendance.ShiftStatus.ISTIRAHAT) return "Istirahat";
        List<FieldTask> activeTasks = fieldTaskService.getTasksByOfficerAndStatus(officerId, FieldTask.TaskStatus.SEDANG_DIKERJAKAN);
        if (!activeTasks.isEmpty()) return "Sedang Bertugas";
        return "Siap Bertugas";
    }

    private List<Map<String, Object>> buildPetugasList(String agencyId) {
        List<User> realPetugas;
        if (agencyId != null) {
            realPetugas = userRepository.findByRoleAndAgencyAgencyId(User.Role.PETUGAS, agencyId);
        } else {
            realPetugas = userService.findByRole(User.Role.PETUGAS);
        }
        return realPetugas.stream().map(p -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", p.getUserId());
            m.put("nama", p.getFullName());
            UserProfile profile = userProfileRepository.findByUserUserId(p.getUserId()).orElse(null);
            m.put("nip", profile != null && profile.getNip() != null ? profile.getNip() : "-");
            m.put("statusKetersediaan", computeOfficerStatus(p.getUserId()));
            m.put("wilayahTugas", profile != null && profile.getWilayahTugas() != null
                    ? profile.getWilayahTugas().getRegionName() : "-");
            m.put("tugasAktif", (int) fieldTaskService.getTasksByOfficerAndStatus(p.getUserId(), FieldTask.TaskStatus.SEDANG_DIKERJAKAN).size());
            m.put("kontak", p.getEmail());
            return m;
        }).collect(Collectors.toList());
    }

    @GetMapping("/admin/dinas/queue")
    public String adminDinasQueue(
            Model model,
            HttpSession session,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page
    ) {
        // SESSION CHECK
        if (ControllerHelper.requireAgencySession(session) == null) return "redirect:/admin/login";

        String agencyId = ControllerHelper.getSessionAgencyId(session);
        String agencyName = ControllerHelper.getSessionAgencyName(session);

        model.addAttribute("dinasName", agencyName != null ? agencyName : "Dinas Pekerjaan Umum");
        List<Map<String, Object>> laporanDinas = new ArrayList<>();
        List<Disposition> realDispositions;
        if (agencyId != null) {
            realDispositions = dispositionService.getDispositionsByAgency(agencyId);
        } else {
            realDispositions = dispositionService.getAllDispositions();
        }
        if (!realDispositions.isEmpty()) {
            // DRY: gunakan konstanta DATE_FMT dari ControllerHelper
            for (Disposition d : realDispositions) {
                Map<String, Object> m = new HashMap<>();
                m.put("id", d.getReport() != null ? d.getReport().getReportId() : "-");
                m.put("judul", d.getReport() != null ? (d.getReport().getTicketNumber() != null ? d.getReport().getTicketNumber() : "Laporan") : "Disposisi");
                m.put("kategori", d.getReport() != null && d.getReport().getCategory() != null ? d.getReport().getCategory().getCategoryName() : "Lainnya");
                m.put("pelapor", d.getReport() != null && d.getReport().getReporter() != null ? d.getReport().getReporter().getFullName() : "-");
                m.put("wilayah", d.getReport() != null && d.getReport().getLocationHint() != null ? d.getReport().getLocationHint() : "-");
                m.put("tanggalDisposisi", d.getDispatchedAt() != null ? d.getDispatchedAt().format(ControllerHelper.DATE_FMT) : "-");
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
    public String adminDinasQueueAlias(Model model, HttpSession session,
                                       @RequestParam(value = "page", required = false, defaultValue = "1") int page) {
        return adminDinasQueue(model, session, page);
    }

    @GetMapping("/admin/dinas/penugasan")
    public String adminDinasPenugasan(
            Model model,
            HttpSession session,
            @RequestParam(value = "id", required = false) String id
    ) {
        // SESSION CHECK
        if (ControllerHelper.requireAgencySession(session) == null) return "redirect:/admin/login";
        
        String agencyId = ControllerHelper.getSessionAgencyId(session);
        String agencyName = ControllerHelper.getSessionAgencyName(session);
        model.addAttribute("dinasName", agencyName != null ? agencyName : "Dinas Pekerjaan Umum");

        List<Map<String, Object>> incomingReports = new ArrayList<>();
        List<Disposition> allDisp;
        if (agencyId != null) {
            allDisp = dispositionService.getDispositionsByAgency(agencyId);
        } else {
            allDisp = dispositionService.getAllDispositions();
        }
        // DRY: gunakan konstanta DATE_FMT dari ControllerHelper
        for (Disposition d : allDisp) {
            if (d.getReport() != null) {
                List<FieldTask> existing = fieldTaskService.getTasksByReport(d.getReport().getReportId());
                if (existing.isEmpty()) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", d.getReport().getReportId());
                    m.put("judul", d.getReport().getTicketNumber() != null ? d.getReport().getTicketNumber() : "Laporan");
                    m.put("kategori", d.getReport().getCategory() != null ? d.getReport().getCategory().getCategoryName() : "Lainnya");
                    m.put("prioritas", d.getPriority() != null ? d.getPriority() : "Sedang");
                    m.put("tanggalDisposisi", d.getDispatchedAt() != null ? d.getDispatchedAt().format(ControllerHelper.DATE_FMT) : "-");
                    m.put("wilayah", d.getReport().getLocationHint() != null ? d.getReport().getLocationHint() : "-");
                    m.put("deadline", d.getDeadline() != null ? d.getDeadline().format(ControllerHelper.DATETIME_FMT) : "-");
                    m.put("instruksiAdmin", d.getInstructions() != null ? d.getInstructions()
                            : (d.getNotes() != null ? d.getNotes() : "-"));
                    m.put("foto", d.getReport().getPhotoBase64() != null ? d.getReport().getPhotoBase64() : dummyReportImage());
                    incomingReports.add(m);
                }
            }
        }

        List<Map<String, Object>> petugasList = buildPetugasList(agencyId);
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
            @RequestParam(value = "catatan", required = false) String catatan,
            HttpSession session
    ) {
        try {
            String adminDinasId = ControllerHelper.requireAgencySession(session);
            if (adminDinasId == null) return "redirect:/admin/login";
            // Ambil userId admin dinas dari session (bukan hardcoded)
            String userId = (String) session.getAttribute("userId");
            if (userId == null) return "redirect:/admin/login";
            fieldTaskService.createTask(id, petugasId, userId);
        } catch (Exception e) {
            log.error("Gagal penugasan petugas: {}", e.getMessage(), e);
        }
        return "redirect:/admin/dinas/penugasan" + (id != null ? "?id=" + id : "");
    }

    @GetMapping("/admin/dinas/penugasan-petugas")
    public String adminDinasPenugasanAlias(Model model, HttpSession session,
                                           @RequestParam(value = "id", required = false) String id) {
        return adminDinasPenugasan(model, session, id);
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
            HttpSession session,
            @RequestParam(value = "id", required = false) String id
    ) {
        // SESSION CHECK
        if (ControllerHelper.requireAnyAdminSession(session) == null) return "redirect:/admin/login";

        List<Map<String, Object>> ticketsInProgress = new ArrayList<>();
        List<FieldTask> realTasks = fieldTaskService.getTasksByStatus(FieldTask.TaskStatus.SEDANG_DIKERJAKAN);
        if (!realTasks.isEmpty()) {
            for (FieldTask t : realTasks) {
                Map<String, Object> m = new HashMap<>();
                m.put("id", t.getTaskId());
                m.put("judul", t.getReport() != null ? (t.getReport().getTicketNumber() != null ? t.getReport().getTicketNumber() : "Laporan") : "Tugas");
                m.put("kategori", t.getReport() != null && t.getReport().getCategory() != null ? t.getReport().getCategory().getCategoryName() : "Lainnya");
                String prioritas = "Sedang";
                Optional<Disposition> disp = dispositionService.getDispositionByReportId(t.getReport().getReportId());
                if (disp.isPresent() && disp.get().getPriority() != null) {
                    prioritas = disp.get().getPriority();
                }
                m.put("prioritas", prioritas);
                m.put("pelapor", t.getReport() != null && t.getReport().getReporter() != null ? t.getReport().getReporter().getFullName() : "-");
                String deadlineStr = "-";
                String slaStatus = "-";
                String slaId = null;
                if (t.getSlaRecord() != null) {
                    slaId = t.getSlaRecord().getSlaId();
                    if (t.getSlaRecord().getSlaDeadlineAt() != null) {
                        deadlineStr = t.getSlaRecord().getSlaDeadlineAt().format(ControllerHelper.DATETIME_FMT);
                    }
                    slaStatus = t.getSlaRecord().getCurrentStatus() != null
                            ? t.getSlaRecord().getCurrentStatus().name() : "-";
                }
                m.put("deadline", deadlineStr);
                m.put("slaStatus", slaStatus);
                m.put("slaId", slaId != null ? slaId : "");
                m.put("foto", dummyReportImage());
                List<Map<String, Object>> ph = new ArrayList<>();
                if (t.getStartedAt() != null) {
                    ph.add(Map.of("tanggal", t.getStartedAt().format(ControllerHelper.DATE_FMT), "petugas",
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
            String targetId = id.trim();
            for (Map<String, Object> t : ticketsInProgress) {
                Object tid = t.get("id");
                if (tid != null && targetId.equals(String.valueOf(tid))) {
                    selected = t;
                    break;
                }
            }
            if (selected == null && !ticketsInProgress.isEmpty()) selected = ticketsInProgress.get(0);
        } else if (!ticketsInProgress.isEmpty()) {
            selected = ticketsInProgress.get(0);
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
    public String adminDinasProgressAlias(Model model, HttpSession session,
                                          @RequestParam(value = "id", required = false) String id) {
        return adminDinasProgress(model, session, id);
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
            HttpSession session,
            @RequestParam(value = "id", required = false) String id
    ) {
        // SESSION CHECK
        if (ControllerHelper.requireAnyAdminSession(session) == null) return "redirect:/admin/login";

        List<Map<String, Object>> ticketsReady = new ArrayList<>();
        List<FieldTask> realTasks = fieldTaskService.getTasksByStatus(FieldTask.TaskStatus.SELESAI);
        if (!realTasks.isEmpty()) {
            // DRY: gunakan konstanta DATE_FMT dari ControllerHelper
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
                if (t.getStartedAt() != null) ph.add(Map.of("tanggal", t.getStartedAt().format(ControllerHelper.DATE_FMT), "keterangan", "Pengerjaan dimulai"));
                if (t.getCompletedAt() != null) ph.add(Map.of("tanggal", t.getCompletedAt().format(ControllerHelper.DATE_FMT), "keterangan", "Pengerjaan selesai"));
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
    public String adminDinasCloseAlias(Model model, HttpSession session,
                                       @RequestParam(value = "id", required = false) String id) {
        return adminDinasClose(model, session, id);
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
            HttpSession session,
            @RequestParam(value = "id", required = false) String id
    ) {
        // SESSION CHECK
        if (ControllerHelper.requireAnyAdminSession(session) == null) return "redirect:/admin/login";

        List<DisputeRecord> realDisputes = disputeService.getPendingDisputes();
        // DRY: gunakan konstanta DATE_FMT dari ControllerHelper
        List<Map<String, Object>> disputes = realDisputes.stream().map(d -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", d.getDisputeId());
            m.put("ticketId", d.getReport() != null ? d.getReport().getReportId() : "-");
            m.put("judul", d.getReasonText() != null ? d.getReasonText() : "Sengketa #" + d.getDisputeId().substring(0, 8));
            m.put("statusSengketa", d.getResolution() == null ? "Menunggu Tinjauan" : "Selesai");
            m.put("prioritas", "Sedang");
            m.put("tanggalSengketa", d.getFiledAt() != null ? d.getFiledAt().format(ControllerHelper.DATE_FMT) : "-");
            m.put("pelapor", d.getReport() != null && d.getReport().getReporter() != null ? d.getReport().getReporter().getFullName() : "-");
            m.put("tanggalLaporan", "-");
            m.put("tanggalSelesai", d.getResolvedAt() != null ? d.getResolvedAt().format(ControllerHelper.DATE_FMT) : "-");
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

        if (selected == null && disputes.isEmpty()) {
            return "redirect:/admin/dinas/dashboard";
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
        String adminId = ControllerHelper.requireAnyAdminSession(session);
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

    // ==========================================
    // ADMIN DINAS — KELOLA PETUGAS
    // ==========================================

    @GetMapping("/admin/dinas/petugas")
    public String adminDinasPetugas(Model model, HttpSession session) {
        if (ControllerHelper.requireAgencySession(session) == null) return "redirect:/admin/login";

        String agencyId = ControllerHelper.getSessionAgencyId(session);
        String agencyName = ControllerHelper.getSessionAgencyName(session);
        model.addAttribute("dinasName", agencyName != null ? agencyName : "Dinas Pekerjaan Umum");

        List<Map<String, Object>> petugasList = buildPetugasList(agencyId);
        model.addAttribute("petugasList", petugasList);
        model.addAttribute("createPetugasDTO", new CreatePetugasDTO());
        List<Region> regions = regionRepository.findAll();
        model.addAttribute("regions", regions);
        return "admin/dinas/petugas";
    }

    @PostMapping("/admin/dinas/petugas/create")
    public String adminDinasCreatePetugas(
            @ModelAttribute CreatePetugasDTO dto,
            RedirectAttributes redirectAttributes,
            HttpSession session
    ) {
        if (ControllerHelper.requireAnyAdminSession(session) == null) return "redirect:/admin/login";

        if (dto.getPassword() == null || dto.getPassword().length() < 6) {
            redirectAttributes.addFlashAttribute("error", "Password minimal 6 karakter");
            return "redirect:/admin/dinas/petugas";
        }

        String agencyId = ControllerHelper.getSessionAgencyId(session);
        if (agencyId != null) {
            dto.setAgencyId(agencyId);
        }

        try {
            userService.createPetugas(dto);
            redirectAttributes.addFlashAttribute("success", "Petugas " + dto.getFullName() + " berhasil dibuat");
        } catch (Exception e) {
            log.error("Gagal buat petugas: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Gagal membuat petugas: " + e.getMessage());
        }
        return "redirect:/admin/dinas/petugas";
    }

    @PostMapping("/admin/dinas/petugas/update-profile")
    public String adminDinasUpdatePetugasProfile(
            @RequestParam("petugasId") String petugasId,
            @RequestParam(value = "nip", required = false) String nip,
            @RequestParam(value = "wilayahTugasRegionId", required = false) String wilayahTugasRegionId,
            RedirectAttributes redirectAttributes,
            HttpSession session
    ) {
        if (ControllerHelper.requireAnyAdminSession(session) == null) return "redirect:/admin/login";

        try {
            UserProfile profile = userProfileRepository.findByUserUserId(petugasId).orElse(null);
            if (profile == null) {
                User petugas = userService.findById(petugasId)
                        .orElseThrow(() -> new RuntimeException("Petugas tidak ditemukan"));
                profile = new UserProfile();
                profile.setUser(petugas);
            }
            if (nip != null && !nip.isBlank()) {
                profile.setNip(nip);
            }
            if (wilayahTugasRegionId != null && !wilayahTugasRegionId.isBlank()) {
                Region wilayah = regionRepository.findById(wilayahTugasRegionId)
                        .orElseThrow(() -> new RuntimeException("Wilayah tidak ditemukan"));
                profile.setWilayahTugas(wilayah);
            }
            userProfileRepository.save(profile);
            redirectAttributes.addFlashAttribute("success", "Profil petugas berhasil diperbarui");
        } catch (Exception e) {
            log.error("Gagal update profil petugas: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Gagal update profil: " + e.getMessage());
        }
        return "redirect:/admin/dinas/petugas";
    }

    // ==========================================
    // ADMIN DINAS — PAUSE / RESUME SLA (FR-JDA)
    // ==========================================

    @PostMapping("/admin/dinas/pause-sla")
    public String adminDinasPauseSla(
            @RequestParam(value = "taskId", required = false) String taskId,
            @RequestParam(value = "reason", required = false) String reason,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        String adminId = ControllerHelper.requireAnyAdminSession(session);
        if (adminId == null) return "redirect:/admin/login";

        try {
            FieldTask task = fieldTaskService.getTaskById(taskId)
                    .orElseThrow(() -> new RuntimeException("Task tidak ditemukan"));
            if (task.getSlaRecord() != null) {
                slaRecordService.pauseSla(task.getSlaRecord().getSlaId(), reason, adminId);
            }
            task.setTaskStatus(FieldTask.TaskStatus.TERTUNDA);
            fieldTaskService.getTaskById(taskId); // trigger save via startTask
            redirectAttributes.addFlashAttribute("success", "SLA berhasil dijeda");
        } catch (Exception e) {
            log.error("Gagal pause SLA: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Gagal pause SLA: " + e.getMessage());
        }
        return "redirect:/admin/dinas/progress" + (taskId != null ? "?id=" + taskId : "");
    }

    @PostMapping("/admin/dinas/resume-sla")
    public String adminDinasResumeSla(
            @RequestParam(value = "taskId", required = false) String taskId,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        String adminId = ControllerHelper.requireAnyAdminSession(session);
        if (adminId == null) return "redirect:/admin/login";

        try {
            FieldTask task = fieldTaskService.getTaskById(taskId)
                    .orElseThrow(() -> new RuntimeException("Task tidak ditemukan"));
            if (task.getSlaRecord() != null) {
                slaRecordService.resumeSla(task.getSlaRecord().getSlaId());
            }
            task.setTaskStatus(FieldTask.TaskStatus.SEDANG_DIKERJAKAN);
            fieldTaskService.startTask(taskId, null, null);
            redirectAttributes.addFlashAttribute("success", "SLA berhasil dilanjutkan");
        } catch (Exception e) {
            log.error("Gagal resume SLA: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Gagal resume SLA: " + e.getMessage());
        }
        return "redirect:/admin/dinas/progress" + (taskId != null ? "?id=" + taskId : "");
    }

    // DRY: didelegasikan ke ControllerHelper — tidak ada duplikasi dengan AdminPusatController
    private String dummyReportImage() {
        return ControllerHelper.dummyReportImage();
    }
}
