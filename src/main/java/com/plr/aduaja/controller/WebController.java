package com.plr.aduaja.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDateTime;
import java.util.*;

@Controller
public class WebController {

    // ==================== GENERAL ====================
    @GetMapping("/")
    public String halamanAwal() {
        return "redirect:/warga/login";
    }

    // ==================== WARGA ROUTES ====================

    // Login & Register
    @GetMapping("/warga/login")
    public String loginWarga() {
        return "warga/login";
    }

    @PostMapping("/warga/login")
    public String postLoginWarga(@RequestParam String email, @RequestParam String password, Model model) {
        // Simple login - redirect to dashboard
        // In production, validate credentials
        model.addAttribute("user", createDummyUser("Warga", email));
        return "redirect:/warga/dashboard";
    }

    @PostMapping("/warga/register")
    public String postRegisterWarga(@RequestParam String name, @RequestParam String email,
                                     @RequestParam String password, @RequestParam String nik,
                                     @RequestParam String phone, Model model) {
        // Register user
        return "redirect:/warga/verify-otp";
    }

    @PostMapping("/warga/verify-otp")
    public String verifyOtpWarga(@RequestParam String otp, Model model) {
        model.addAttribute("user", createDummyUser("Warga", "user@email.com"));
        return "redirect:/warga/dashboard";
    }

    @PostMapping("/warga/logout")
    public String logoutWarga() {
        return "redirect:/warga/login";
    }

    // Dashboard & Main Pages
    @GetMapping("/warga/dashboard")
    public String dashboardWarga(Model model) {
        model.addAttribute("user", createDummyUser("Warga", "warga@email.com"));
        model.addAttribute("stats", createWargaStats());
        model.addAttribute("recentReports", createDummyReports());
        return "warga/dashboard";
    }

    @GetMapping("/warga/create-report")
    public String createReportWarga(Model model) {
        model.addAttribute("user", createDummyUser("Warga", "warga@email.com"));
        return "warga/create-report";
    }

    @PostMapping("/warga/create-report")
    public String postCreateReportWarga(Model model) {
        return "redirect:/warga/report-history";
    }

    @GetMapping("/warga/report-history")
    public String reportHistoryWarga(@RequestParam(required = false) String status,
                                      @RequestParam(required = false) String q, Model model) {
        model.addAttribute("user", createDummyUser("Warga", "warga@email.com"));
        model.addAttribute("reports", createDummyReports());
        model.addAttribute("statuses", Arrays.asList("Semua", "Baru", "Diproses", "Selesai", "Ditolak"));
        model.addAttribute("statusOptions", Arrays.asList("Semua", "Baru", "Diproses", "Selesai", "Ditolak"));
        model.addAttribute("filterStatus", status != null ? status : "Semua");
        model.addAttribute("searchQuery", q != null ? q : "");
        model.addAttribute("totalCount", 3);

        Map<String, Integer> statusCounts = new HashMap<>();
        statusCounts.put("Semua", 3);
        statusCounts.put("Baru", 1);
        statusCounts.put("Diproses", 1);
        statusCounts.put("Selesai", 1);
        statusCounts.put("Ditolak", 0);
        model.addAttribute("statusCounts", statusCounts);

        return "warga/report-history";
    }

    @GetMapping("/warga/report-detail")
    public String reportDetailWarga(@RequestParam String id, Model model) {
        model.addAttribute("user", createDummyUser("Warga", "warga@email.com"));
        model.addAttribute("report", createDummyReport(id));
        return "warga/report-detail";
    }

    @PostMapping("/warga/report/confirm")
    public String confirmReportWarga(@RequestParam String id) {
        return "redirect:/warga/report-detail?id=" + id;
    }

    @PostMapping("/warga/report/dispute")
    public String disputeReportWarga(@RequestParam String id) {
        return "redirect:/warga/report-detail?id=" + id;
    }

    @GetMapping("/warga/profile")
    public String profileWarga(Model model) {
        model.addAttribute("user", createDummyUser("Warga", "warga@email.com"));
        return "warga/profile";
    }

    @PostMapping("/warga/profile")
    public String postProfileWarga() {
        return "redirect:/warga/dashboard";
    }

    @GetMapping("/warga/notifications")
    public String notificationsWarga(@RequestParam(required = false) String filter, Model model) {
        model.addAttribute("user", createDummyUser("Warga", "warga@email.com"));
        model.addAttribute("notifications", createDummyNotifications());
        return "warga/notifications";
    }

    @GetMapping("/warga/module")
    public String moduleWarga(Model model) {
        model.addAttribute("user", createDummyUser("Warga", "warga@email.com"));
        return "warga/module";
    }

    // ==================== ADMIN ROUTES ====================

    @GetMapping("/admin/login")
    public String loginAdmin() {
        return "admin/login";
    }

    @PostMapping("/admin/login")
    public String postLoginAdmin(@RequestParam String email, @RequestParam String password,
                                  @RequestParam(required = false) String role, Model model) {
        model.addAttribute("user", createDummyUser("Admin", email));
        model.addAttribute("userRole", role != null ? role : "admin_pusat");
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/admin/logout")
    public String logoutAdmin() {
        return "redirect:/admin/login";
    }

    @GetMapping("/admin/dashboard")
    public String dashboardAdmin(Model model) {
        model.addAttribute("user", createDummyUser("Admin", "admin@email.com"));
        model.addAttribute("userRole", "admin_pusat");
        model.addAttribute("stats", createAdminStats());
        return "admin/dashboard";
    }

    @GetMapping("/admin/home")
    public String homeAdmin(Model model) {
        model.addAttribute("user", createDummyUser("Admin", "admin@email.com"));
        return "admin/home";
    }

    @GetMapping("/admin/validation-panel")
    public String validationPanelAdmin(Model model) {
        model.addAttribute("user", createDummyUser("Admin", "admin@email.com"));
        return "admin/validation-panel";
    }

    @GetMapping("/admin/disposisi-panel")
    public String disposisiPanelAdmin(Model model) {
        model.addAttribute("user", createDummyUser("Admin", "admin@email.com"));
        return "admin/disposisi-panel";
    }

    @GetMapping("/admin/merge-ticket-panel")
    public String mergeTicketPanelAdmin(Model model) {
        model.addAttribute("user", createDummyUser("Admin", "admin@email.com"));
        return "admin/merge-ticket-panel";
    }

    @GetMapping("/admin/sengketa-panel")
    public String sengketaPanelAdmin(Model model) {
        model.addAttribute("user", createDummyUser("Admin", "admin@email.com"));
        return "admin/sengketa-panel";
    }

    @GetMapping("/admin/laporan-queue")
    public String laporanQueueAdmin(Model model) {
        model.addAttribute("user", createDummyUser("Admin", "admin@email.com"));
        return "admin/laporan-queue";
    }

    @GetMapping("/admin/dinas/dinas-dashboard")
    public String dinasDashboardAdmin(Model model) {
        model.addAttribute("user", createDummyUser("Admin Dinas", "admin@email.com"));
        return "admin/dinas/dinas-dashboard";
    }

    @GetMapping("/admin/dinas/dinas-queue")
    public String dinasQueueAdmin(Model model) {
        model.addAttribute("user", createDummyUser("Admin Dinas", "admin@email.com"));
        return "admin/dinas/dinas-queue";
    }

    @GetMapping("/admin/dinas/penugasan-petugas")
    public String penugasanPetugasAdmin(Model model) {
        model.addAttribute("user", createDummyUser("Admin Dinas", "admin@email.com"));
        return "admin/dinas/penugasan-petugas";
    }

    @GetMapping("/admin/dinas/progress-update")
    public String progressUpdateAdmin(Model model) {
        model.addAttribute("user", createDummyUser("Admin Dinas", "admin@email.com"));
        return "admin/dinas/progress-update";
    }

    @GetMapping("/admin/dinas/close-ticket")
    public String closeTicketAdmin(Model model) {
        model.addAttribute("user", createDummyUser("Admin Dinas", "admin@email.com"));
        return "admin/dinas/close-ticket";
    }

    // ==================== PETUGAS ROUTES ====================

    @GetMapping("/petugas/login")
    public String loginPetugas() {
        return "petugas/login";
    }

    @PostMapping("/petugas/login")
    public String postLoginPetugas(@RequestParam String email, @RequestParam String password, Model model) {
        model.addAttribute("user", createDummyUser("Petugas", email));
        model.addAttribute("user.dinas", "Dinas PU");
        return "redirect:/petugas/dashboard";
    }

    @PostMapping("/petugas/logout")
    public String logoutPetugas() {
        return "redirect:/petugas/login";
    }

    @GetMapping("/petugas/dashboard")
    public String dashboardPetugas(Model model) {
        Map<String, Object> user = createDummyUser("Petugas", "petugas@email.com");
        user.put("dinas", "Dinas PU");
        model.addAttribute("user", user);
        model.addAttribute("attendance", createDummyAttendance());
        return "petugas/dashboard";
    }

    @GetMapping("/petugas/tasks")
    public String tasksPetugas(Model model) {
        Map<String, Object> user = createDummyUser("Petugas", "petugas@email.com");
        user.put("dinas", "Dinas PU");
        model.addAttribute("user", user);
        model.addAttribute("tasks", createDummyTasks());
        return "petugas/tasks";
    }

    @GetMapping("/petugas/task-detail")
    public String taskDetailPetugas(@RequestParam String id, Model model) {
        Map<String, Object> user = createDummyUser("Petugas", "petugas@email.com");
        user.put("dinas", "Dinas PU");
        model.addAttribute("user", user);
        model.addAttribute("task", createDummyTaskDetail(id));
        return "petugas/task-detail";
    }

    @GetMapping("/petugas/task-execution")
    public String taskExecutionPetugas(@RequestParam String id, Model model) {
        Map<String, Object> user = createDummyUser("Petugas", "petugas@email.com");
        user.put("dinas", "Dinas PU");
        model.addAttribute("user", user);
        model.addAttribute("task", createDummyTaskDetail(id));
        return "petugas/task-execution";
    }

    @PostMapping("/petugas/status")
    public String statusPetugas(@RequestParam String status) {
        return "redirect:/petugas/dashboard";
    }

    @PostMapping("/petugas/check-in")
    public String checkInPetugas() {
        return "redirect:/petugas/dashboard";
    }

    @PostMapping("/petugas/check-out")
    public String checkOutPetugas() {
        return "redirect:/petugas/dashboard";
    }

    @GetMapping("/petugas/reports")
    public String reportsPetugas(Model model) {
        Map<String, Object> user = createDummyUser("Petugas", "petugas@email.com");
        user.put("dinas", "Dinas PU");
        model.addAttribute("user", user);
        model.addAttribute("reports", createDummyReports());
        return "petugas/reports";
    }

    @GetMapping("/petugas/history")
    public String historyPetugas(Model model) {
        Map<String, Object> user = createDummyUser("Petugas", "petugas@email.com");
        user.put("dinas", "Dinas PU");
        model.addAttribute("user", user);
        model.addAttribute("history", createDummyHistory());
        return "petugas/history";
    }

    @GetMapping("/petugas/attendance-history")
    public String attendanceHistoryPetugas(Model model) {
        Map<String, Object> user = createDummyUser("Petugas", "petugas@email.com");
        user.put("dinas", "Dinas PU");
        model.addAttribute("user", user);
        model.addAttribute("attendanceRecords", createDummyAttendanceHistory());
        return "petugas/attendance-history";
    }

    @GetMapping("/petugas/home")
    public String homePetugas(Model model) {
        Map<String, Object> user = createDummyUser("Petugas", "petugas@email.com");
        user.put("dinas", "Dinas PU");
        model.addAttribute("user", user);
        return "petugas/home";
    }

    // ==================== HELPER METHODS ====================

    private Map<String, Object> createDummyUser(String role, String email) {
        Map<String, Object> user = new HashMap<>();
        user.put("id", "1");
        user.put("name", "User " + role);
        user.put("email", email);
        user.put("phone", "08123456789");
        user.put("role", role);
        return user;
    }

    private List<Map<String, Object>> createWargaStats() {
        List<Map<String, Object>> stats = new ArrayList<>();
        stats.add(createStat("Laporan Baru", "12", "bg-blue-100", "text-blue-600", "file-text"));
        stats.add(createStat("Diproses", "5", "bg-yellow-100", "text-yellow-600", "clock"));
        stats.add(createStat("Selesai", "23", "bg-green-100", "text-green-600", "check-circle"));
        stats.add(createStat("Ditolak", "2", "bg-red-100", "text-red-600", "x-circle"));
        return stats;
    }

    private List<Map<String, Object>> createAdminStats() {
        List<Map<String, Object>> stats = new ArrayList<>();
        stats.add(createStat("Antrian", "45", "bg-blue-100", "text-blue-600", "file-text"));
        stats.add(createStat("Validasi", "12", "bg-yellow-100", "text-yellow-600", "check-circle-2"));
        stats.add(createStat("Disposisi", "8", "bg-green-100", "text-green-600", "send"));
        stats.add(createStat("Duplikat", "3", "bg-red-100", "text-red-600", "copy"));
        return stats;
    }

    private Map<String, Object> createStat(String title, String value, String bgColor, String color, String icon) {
        Map<String, Object> stat = new HashMap<>();
        stat.put("title", title);
        stat.put("value", value);
        stat.put("bgColor", bgColor);
        stat.put("color", color);
        stat.put("icon", icon);
        stat.put("label", title);
        stat.put("count", value);
        return stat;
    }

    private List<Map<String, Object>> createDummyReports() {
        List<Map<String, Object>> reports = new ArrayList<>();
        reports.add(createReport("1", "Jalan Berlubang di Jl. Sudirman", "Infrastruktur Jalan", "Jl. Sudirman no. 123", "Baru", "bg-blue-100 text-blue-800"));
        reports.add(createReport("2", "Lampu Jalan Mati", "Listrik Publik", "Jl. Ahmad Yani", "Diproses", "bg-yellow-100 text-yellow-800"));
        reports.add(createReport("3", "Saluran Air Tersumbat", "Sistem Drainase", "Jl. Gajah Mada", "Selesai", "bg-green-100 text-green-800"));
        return reports;
    }

    private Map<String, Object> createReport(String id, String title, String category, String location, String status, String statusColor) {
        Map<String, Object> report = new HashMap<>();
        report.put("id", id);
        report.put("title", title);
        report.put("category", category);
        report.put("location", location);
        report.put("status", status);
        report.put("statusColor", statusColor);
        report.put("date", LocalDateTime.now());
        report.put("description", "Laporan detail tentang " + title);
        report.put("images", new String[]{"image1.jpg", "image2.jpg"});
        return report;
    }

    private Map<String, Object> createDummyReport(String id) {
        return createReport(id, "Jalan Berlubang", "Infrastruktur Jalan", "Jl. Sudirman", "Diproses", "bg-yellow-100 text-yellow-800");
    }

    private List<Map<String, Object>> createDummyNotifications() {
        List<Map<String, Object>> notifications = new ArrayList<>();
        Map<String, Object> notif1 = new HashMap<>();
        notif1.put("id", "1");
        notif1.put("title", "Laporan Diterima");
        notif1.put("message", "Laporan Anda telah diterima oleh admin");
        notif1.put("reportId", "1");
        notif1.put("timestamp", LocalDateTime.now());
        notif1.put("isRead", false);
        notifications.add(notif1);

        Map<String, Object> notif2 = new HashMap<>();
        notif2.put("id", "2");
        notif2.put("title", "Status Update");
        notif2.put("message", "Laporan Anda sedang diproses");
        notif2.put("reportId", "2");
        notif2.put("timestamp", LocalDateTime.now().minusHours(2));
        notif2.put("isRead", true);
        notifications.add(notif2);

        return notifications;
    }

    private Map<String, Object> createDummyAttendance() {
        Map<String, Object> attendance = new HashMap<>();
        attendance.put("currentStatus", "Siap Bertugas");
        attendance.put("checkedIn", true);
        attendance.put("checkInTime", LocalDateTime.now());
        attendance.put("location", "Kantor Dinas PU");
        return attendance;
    }

    private List<Map<String, Object>> createDummyTasks() {
        List<Map<String, Object>> tasks = new ArrayList<>();
        tasks.add(createTask("1", "Laporan Jalan Berlubang", "Urgent", "Jl. Sudirman", "Pending"));
        tasks.add(createTask("2", "Perbaikan Lampu Jalan", "High", "Jl. Ahmad Yani", "In Progress"));
        tasks.add(createTask("3", "Pembersihan Saluran", "Medium", "Jl. Gajah Mada", "Completed"));
        return tasks;
    }

    private Map<String, Object> createTask(String id, String title, String priority, String location, String status) {
        Map<String, Object> task = new HashMap<>();
        task.put("id", id);
        task.put("title", title);
        task.put("priority", priority);
        task.put("location", location);
        task.put("status", status);
        task.put("description", "Detail tugas " + title);
        task.put("deadline", LocalDateTime.now().plusDays(1));
        return task;
    }

    private Map<String, Object> createDummyTaskDetail(String id) {
        return createTask(id, "Perbaikan Jalan Berlubang", "Urgent", "Jl. Sudirman", "In Progress");
    }

    private List<Map<String, Object>> createDummyHistory() {
        List<Map<String, Object>> history = new ArrayList<>();
        Map<String, Object> h1 = new HashMap<>();
        h1.put("id", "1");
        h1.put("taskTitle", "Perbaikan Jalan");
        h1.put("status", "Completed");
        h1.put("date", LocalDateTime.now().minusDays(1));
        history.add(h1);

        Map<String, Object> h2 = new HashMap<>();
        h2.put("id", "2");
        h2.put("taskTitle", "Perbaikan Lampu");
        h2.put("status", "Completed");
        h2.put("date", LocalDateTime.now().minusDays(3));
        history.add(h2);

        return history;
    }

    private List<Map<String, Object>> createDummyAttendanceHistory() {
        List<Map<String, Object>> records = new ArrayList<>();
        Map<String, Object> r1 = new HashMap<>();
        r1.put("date", LocalDateTime.now().minusDays(1));
        r1.put("checkIn", LocalDateTime.now().minusDays(1).withHour(8).withMinute(0));
        r1.put("checkOut", LocalDateTime.now().minusDays(1).withHour(17).withMinute(0));
        r1.put("status", "Present");
        records.add(r1);

        return records;
    }
}