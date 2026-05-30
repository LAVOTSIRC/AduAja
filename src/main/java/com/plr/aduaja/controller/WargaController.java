package com.plr.aduaja.controller;

import lombok.extern.slf4j.Slf4j;
import com.plr.aduaja.dto.CreateReportDTO;
import com.plr.aduaja.model.Report;
import com.plr.aduaja.model.Report.ReportStatus;
import com.plr.aduaja.model.User;
import com.plr.aduaja.repository.ReportCategoryRepository;
import com.plr.aduaja.service.NotificationService;
import com.plr.aduaja.service.ReportService;
import com.plr.aduaja.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Slf4j
@Controller
public class WargaController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ReportCategoryRepository reportCategoryRepository;

    // PERBAIKAN ABSTRAKSI: Controller tidak boleh inject Repository langsung!
    // Sebelumnya ada: @Autowired UserRepository userRepository;
    // Kini findByRole() dipanggil via UserService (Abstraction principle)

    @GetMapping("/warga/module")
    public String wargaModule() {
        return "warga/module";
    }

    @GetMapping("/warga/dashboard")
    public String wargaDashboard(Model model, HttpSession session) {
        String userId = ControllerHelper.requireRole(session, "WARGA");
        if (userId == null) return "redirect:/warga/login";

        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isEmpty()) {
            session.invalidate();
            return "redirect:/warga/login";
        }

        User user = userOpt.get();
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("name", user.getFullName());
        userMap.put("email", user.getEmail());
        userMap.put("id", user.getUserId());
        userMap.put("profilePhotoUrl", user.getUserProfile() != null ? user.getUserProfile().getProfilePhotoUrl() : null);
        model.addAttribute("user", userMap);

        List<Report> dbReports = reportService.getReportsByWarga(userId);
        long total = dbReports.size();
        long diproses = dbReports.stream().filter(r -> r.getStatus() == ReportStatus.DITUGASKAN || r.getStatus() == ReportStatus.SEDANG_DIKERJAKAN).count();
        long selesai = dbReports.stream().filter(r -> r.getStatus() == ReportStatus.SELESAI).count();
        long ditolak = dbReports.stream().filter(r -> r.getStatus() == ReportStatus.DITOLAK).count();
        long menunggu = dbReports.stream().filter(r -> r.getStatus() == ReportStatus.MENUNGGU_VALIDASI).count();

        List<Map<String, Object>> stats = new ArrayList<>();
        stats.add(Map.of(    "icon","file-spreadsheet",   "color","bg-blue-100 text-blue-600",   "count",total,"label","Total Laporan"));
        stats.add(Map.of("icon","clock",        "color","bg-yellow-100 text-yellow-600","count",menunggu,"label","Menunggu"));
        stats.add(Map.of("icon","wrench",       "color","bg-orange-100 text-orange-600","count",diproses,"label","Diproses"));
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
        model.addAttribute("unreadCount", notificationService.countUnreadByUser(userId));
        return "warga/dashboard";
    }

    @GetMapping("/warga/create-report")
    public String wargaCreateReport(Model model, HttpSession session) {
        String userId = ControllerHelper.requireRole(session, "WARGA");
        if (userId == null) return "redirect:/warga/login";
        model.addAttribute("createReportDTO", new CreateReportDTO());
        model.addAttribute("categories", reportCategoryRepository.findByIsActiveTrue());
        return "warga/create-report";
    }

    @PostMapping("/warga/create-report")
    public String wargaCreateReportPost(
            @ModelAttribute CreateReportDTO dto,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        String userId = ControllerHelper.requireRole(session, "WARGA");
        if (userId == null) return "redirect:/warga/login";

        try {
            Report report = reportService.createReport(dto, userId);
            // ABSTRAKSI: userService.findByRole() gantikan userRepository.findByRole()
            List<User> admins = userService.findByRole(User.Role.ADMIN_PUSAT);
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
            log.error("Gagal buat laporan oleh user {}: {}", userId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Gagal mengirim laporan: " + e.getMessage());
            return "redirect:/warga/create-report";
        }
    }

    @GetMapping("/warga/report-history")
    public String wargaReportHistory(
            Model model,
            HttpSession session,
            @RequestParam(value = "status", required = false, defaultValue = "Semua") String filterStatus,
            @RequestParam(value = "q", required = false, defaultValue = "") String searchQuery,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size
    ) {
        String userId = ControllerHelper.requireRole(session, "WARGA");
        if (userId == null) return "redirect:/warga/login";

        List<Report> dbReports = reportService.getReportsByWarga(userId);
        List<Map<String, Object>> allReports = new ArrayList<>();
        // DRY: gunakan konstanta DATE_FMT dari ControllerHelper
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

            String icon = "file-spreadsheet";
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
            boolean matchQuery = searchQuery.isBlank()
                    || r.get("title").toString().toLowerCase().contains(searchQuery.toLowerCase())
                    || r.get("id").toString().toLowerCase().contains(searchQuery.toLowerCase())
                    || r.get("category").toString().toLowerCase().contains(searchQuery.toLowerCase());
            if (matchStatus && matchQuery) filtered.add(r);
        }

        List<Map<String, Object>> statusOptions = new ArrayList<>();
        Map<String, Object> allOpt = new LinkedHashMap<>();
        allOpt.put("name", "Semua");
        allOpt.put("count", allReports.size());
        statusOptions.add(allOpt);

        String[] allLabels = {
            "Menunggu","Perlu Revisi","Ditolak","Divalidasi",
            "Didisposisi","Ditugaskan","Diproses","Tertunda",
            "Menunggu Konfirmasi","Selesai","Sengketa","Ditutup"
        };
        for (String label : allLabels) {
            int cnt = 0;
            for (Map<String, Object> r : allReports) {
                if (label.equals(r.get("status"))) cnt++;
            }
            Map<String, Object> opt = new LinkedHashMap<>();
            opt.put("name", label);
            opt.put("count", cnt);
            statusOptions.add(opt);
        }

        int totalCount = filtered.size();
        int totalPages = (int) Math.ceil((double) totalCount / size);
        if (totalPages < 1) totalPages = 1;
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;
        int startIndex = (page - 1) * size + 1;
        int endIndex = Math.min(page * size, totalCount);
        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, totalCount);
        List<Map<String, Object>> paged = totalCount > 0 && fromIndex < totalCount
                ? filtered.subList(fromIndex, toIndex)
                : new ArrayList<>();

        int maxVisiblePages = 7;
        int startPage = Math.max(1, page - 3);
        int endPage = Math.min(totalPages, startPage + maxVisiblePages - 1);
        if (endPage - startPage < maxVisiblePages - 1) {
            startPage = Math.max(1, endPage - maxVisiblePages + 1);
        }
        List<Integer> pageNumbers = new ArrayList<>();
        for (int i = startPage; i <= endPage; i++) {
            pageNumbers.add(i);
        }

        model.addAttribute("reports",      paged);
        model.addAttribute("totalCount",   totalCount);
        model.addAttribute("allCount",     allReports.size());
        model.addAttribute("filterStatus", filterStatus);
        model.addAttribute("searchQuery",  searchQuery);
        model.addAttribute("statusOptions",statusOptions);
        model.addAttribute("page",         page);
        model.addAttribute("totalPages",   totalPages);
        model.addAttribute("startIndex",   startIndex);
        model.addAttribute("endIndex",     endIndex);
        model.addAttribute("pageNumbers",  pageNumbers);
        return "warga/report-history";
    }

    @GetMapping("/warga/report-detail")
    public String wargaReportDetail(
            Model model,
            HttpSession session,
            @RequestParam(value = "id", required = false) String id,
            @RequestParam(value = "revPage", required = false, defaultValue = "1") int revPage,
            @RequestParam(value = "revSize", required = false, defaultValue = "5") int revSize
    ) {
        String userId = ControllerHelper.requireRole(session, "WARGA");
        if (userId == null) return "redirect:/warga/login";
        if (id == null || id.isBlank()) return "redirect:/warga/report-history";

        Report report = reportService.findById(id).orElse(null);
        if (report == null) return "redirect:/warga/report-history";
        if (revSize < 1) revSize = 5;

        // DRY: gunakan konstanta DATETIME_FMT dari ControllerHelper
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
        reportMap.put("submittedAt", report.getSubmittedAt() != null ? report.getSubmittedAt().format(ControllerHelper.DATETIME_FMT) : "-");
        reportMap.put("createdDate", report.getSubmittedAt() != null ? report.getSubmittedAt().format(ControllerHelper.DATETIME_FMT) : "-");
        reportMap.put("date", report.getSubmittedAt() != null ? report.getSubmittedAt().toLocalDate() : java.time.LocalDate.now());
        reportMap.put("slaDeadline", "-");

        List<?> allRevisions = report.getRevisions() != null ? new ArrayList<>(report.getRevisions()) : new ArrayList<>();
        int revisionTotalCount = allRevisions.size();
        int revisionTotalPages = (int) Math.ceil((double) revisionTotalCount / revSize);
        if (revisionTotalPages < 1) revisionTotalPages = 1;
        if (revPage < 1) revPage = 1;
        if (revPage > revisionTotalPages) revPage = revisionTotalPages;
        int revisionFromIndex = (revPage - 1) * revSize;
        int revisionToIndex = Math.min(revisionFromIndex + revSize, revisionTotalCount);
        List<?> pagedRevisions = revisionTotalCount > 0 && revisionFromIndex < revisionTotalCount
                ? allRevisions.subList(revisionFromIndex, revisionToIndex)
                : new ArrayList<>();

        int maxVisiblePages = 7;
        int revisionStartPage = Math.max(1, revPage - 3);
        int revisionEndPage = Math.min(revisionTotalPages, revisionStartPage + maxVisiblePages - 1);
        if (revisionEndPage - revisionStartPage < maxVisiblePages - 1) {
            revisionStartPage = Math.max(1, revisionEndPage - maxVisiblePages + 1);
        }
        List<Integer> revisionPageNumbers = new ArrayList<>();
        for (int i = revisionStartPage; i <= revisionEndPage; i++) {
            revisionPageNumbers.add(i);
        }

        reportMap.put("revisions", pagedRevisions);
        String cc = switch (report.getStatus()) {
            case MENUNGGU_VALIDASI -> "bg-gray-100 text-gray-700";
            case DIVALIDASI, DITUGASKAN, SEDANG_DIKERJAKAN -> "bg-yellow-100 text-yellow-700";
            case SELESAI -> "bg-green-100 text-green-700";
            case DITOLAK -> "bg-red-100 text-red-700";
            default -> "bg-gray-100 text-gray-700";
        };
        reportMap.put("statusColor", cc);

        model.addAttribute("report", reportMap);
        model.addAttribute("revisionPage", revPage);
        model.addAttribute("revisionSize", revSize);
        model.addAttribute("revisionTotalCount", revisionTotalCount);
        model.addAttribute("revisionTotalPages", revisionTotalPages);
        model.addAttribute("revisionStartIndex", revisionTotalCount == 0 ? 0 : revisionFromIndex + 1);
        model.addAttribute("revisionEndIndex", revisionToIndex);
        model.addAttribute("revisionPageNumbers", revisionPageNumbers);
        return "warga/report-detail";
    }

    @GetMapping("/warga/notifications")
    public String wargaNotifications(
            Model model,
            HttpSession session,
            @RequestParam(value = "filter", required = false, defaultValue = "semua") String filter,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size
    ) {
        String userId = ControllerHelper.requireRole(session, "WARGA");
        if (userId == null) return "redirect:/warga/login";

        List<com.plr.aduaja.model.Notification> allNotifs = filter.equals("belum-dibaca")
                ? notificationService.getUnreadNotificationsByUser(userId)
                : notificationService.getNotificationsByUser(userId);
        long unreadCount = notificationService.countUnreadByUser(userId);

        int totalCount = allNotifs.size();
        int totalPages = (int) Math.ceil((double) totalCount / size);
        if (totalPages < 1) totalPages = 1;
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;
        int startIndex = (page - 1) * size + 1;
        int endIndex = Math.min(page * size, totalCount);
        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, totalCount);
        List<com.plr.aduaja.model.Notification> notifs = totalCount > 0 && fromIndex < totalCount
                ? allNotifs.subList(fromIndex, toIndex)
                : new ArrayList<>();

        int maxVisiblePages = 7;
        int startPage = Math.max(1, page - 3);
        int endPage = Math.min(totalPages, startPage + maxVisiblePages - 1);
        if (endPage - startPage < maxVisiblePages - 1) {
            startPage = Math.max(1, endPage - maxVisiblePages + 1);
        }
        List<Integer> pageNumbers = new ArrayList<>();
        for (int i = startPage; i <= endPage; i++) {
            pageNumbers.add(i);
        }

        model.addAttribute("notifications", notifs);
        model.addAttribute("totalCount",  totalCount);
        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("filter",      filter);
        model.addAttribute("page",         page);
        model.addAttribute("totalPages",   totalPages);
        model.addAttribute("startIndex",   startIndex);
        model.addAttribute("endIndex",     endIndex);
        model.addAttribute("pageNumbers",  pageNumbers);
        return "warga/notifications";
    }

    @PostMapping("/warga/notifications/mark-read")
    public String wargaMarkAllRead(HttpSession session) {
        String userId = ControllerHelper.requireRole(session, "WARGA");
        if (userId == null) return "redirect:/warga/login";
        notificationService.markAllAsReadByUser(userId);
        return "redirect:/warga/notifications";
    }

    // ==========================================
    // PRIVATE HELPERS
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
}
