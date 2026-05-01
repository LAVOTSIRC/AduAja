package com.plr.aduaja.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    // ==========================================
    // ROOT REDIRECT
    // ==========================================
    @GetMapping("/")
    public String rootRedirect() {
        // Mengarahkan akses root langsung ke halaman login warga
        return "redirect:/warga/login";
    }

    // ==========================================
    // INDEX & LAYOUTS
    // ==========================================
    @GetMapping("/index")
    public String index() {
        return "index";
    }

    @GetMapping("/layouts/master")
    public String layoutsMaster() {
        return "layouts/master";
    }

    // ==========================================
    // ADMIN ROUTES
    // ==========================================
    @GetMapping("/admin/dashboard")
    public String adminDashboard() { return "admin/dashboard"; }

    @GetMapping("/admin/disposisi-panel")
    public String adminDisposisiPanel() { return "admin/disposisi-panel"; }

    @GetMapping("/admin/home")
    public String adminHome() { return "admin/home"; }

    @GetMapping("/admin/laporan-queue")
    public String adminLaporanQueue() { return "admin/laporan-queue"; }

    @GetMapping("/admin/login")
    public String adminLogin() { return "admin/login"; }

    @GetMapping("/admin/merge-ticket-panel")
    public String adminMergeTicketPanel() { return "admin/merge-ticket-panel"; }

    @GetMapping("/admin/sengketa-panel")
    public String adminSengketaPanel() { return "admin/sengketa-panel"; }

    @GetMapping("/admin/validation-panel")
    public String adminValidationPanel() { return "admin/validation-panel"; }

    // --- ADMIN DINAS SUB-FOLDER ---
    @GetMapping("/admin/dinas/close-ticket")
    public String adminDinasCloseTicket() { return "admin/dinas/close-ticket"; }

    @GetMapping("/admin/dinas/dinas-dashboard")
    public String adminDinasDashboard() { return "admin/dinas/dinas-dashboard"; }

    @GetMapping("/admin/dinas/dinas-queue")
    public String adminDinasQueue() { return "admin/dinas/dinas-queue"; }

    @GetMapping("/admin/dinas/penugasan-petugas")
    public String adminDinasPenugasanPetugas() { return "admin/dinas/penugasan-petugas"; }

    @GetMapping("/admin/dinas/progress-update")
    public String adminDinasProgressUpdate() { return "admin/dinas/progress-update"; }

    // ==========================================
    // PETUGAS ROUTES
    // ==========================================
    @GetMapping("/petugas/attendance-history")
    public String petugasAttendanceHistory() { return "petugas/attendance-history"; }

    @GetMapping("/petugas/dashboard")
    public String petugasDashboard() { return "petugas/dashboard"; }

    @GetMapping("/petugas/history")
    public String petugasHistory() { return "petugas/history"; }

    @GetMapping("/petugas/home")
    public String petugasHome() { return "petugas/home"; }

    @GetMapping("/petugas/login")
    public String petugasLogin() { return "petugas/login"; }

    @GetMapping("/petugas/reports")
    public String petugasReports() { return "petugas/reports"; }

    @GetMapping("/petugas/task-detail")
    public String petugasTaskDetail() { return "petugas/task-detail"; }

    @GetMapping("/petugas/task-execution")
    public String petugasTaskExecution() { return "petugas/task-execution"; }

    @GetMapping("/petugas/tasks")
    public String petugasTasks() { return "petugas/tasks"; }

    // ==========================================
    // WARGA ROUTES
    // ==========================================
    @GetMapping("/warga/create-report")
    public String wargaCreateReport() { return "warga/create-report"; }

    @GetMapping("/warga/dashboard")
    public String wargaDashboard() { return "warga/dashboard"; }

    @GetMapping("/warga/login")
    public String wargaLogin() { return "warga/login"; }

    @GetMapping("/warga/module")
    public String wargaModule() { return "warga/module"; }

    @GetMapping("/warga/notifications")
    public String wargaNotifications() { return "warga/notifications"; }

    @GetMapping("/warga/profile")
    public String wargaProfile() { return "warga/profile"; }

    @GetMapping("/warga/report-detail")
    public String wargaReportDetail() { return "warga/report-detail"; }

    @GetMapping("/warga/report-history")
    public String wargaReportHistory() { return "warga/report-history"; }
}