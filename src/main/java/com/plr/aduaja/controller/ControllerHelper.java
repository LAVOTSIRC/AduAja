package com.plr.aduaja.controller;

import jakarta.servlet.http.HttpSession;
import java.time.format.DateTimeFormatter;

// ============================================================
// DRY PRINCIPLE: Utility class untuk menghindari duplikasi kode
// di semua controller. Method static bisa diakses tanpa instansiasi.
//
// ENKAPSULASI: semua method bersifat package-private atau public static,
// dipanggil dari controller mana saja dalam package ini.
// ============================================================
public final class ControllerHelper {

    // ============================================================
    // DRY: DateTimeFormatter CONSTANTS — dibuat sekali, dipakai semua controller
    // Sebelumnya setiap method membuat new DateTimeFormatter("dd MMM yyyy") sendiri
    // ============================================================
    public static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy");

    public static final DateTimeFormatter DATETIME_FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    public static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("HH:mm");

    public static final String SESSION_AGENCY_ID = "agencyId";
    public static final String SESSION_AGENCY_NAME = "agencyName";

    public static final String SESSION_REGION_ID = "regionId";
    public static final String SESSION_REGION_NAME = "regionName";

    // Private constructor — tidak bisa diinstansiasi
    private ControllerHelper() {}

    // ============================================================
    // DRY: dummyReportImage() — sebelumnya duplikat di AdminPusatController
    // dan AdminDinasController. Sekarang cukup dipanggil dari sini.
    // ============================================================
    public static String dummyReportImage() {
        return "data:image/svg+xml;charset=UTF-8,%3Csvg xmlns='http://www.w3.org/2000/svg' " +
               "width='1200' height='800' viewBox='0 0 1200 800'%3E" +
               "%3Cdefs%3E%3ClinearGradient id='g' x1='0' x2='1' y1='0' y2='1'%3E" +
               "%3Cstop offset='0%25' stop-color='%231d4ed8'/%3E" +
               "%3Cstop offset='100%25' stop-color='%230f766e'/%3E" +
               "%3C/linearGradient%3E%3C/defs%3E" +
               "%3Crect width='1200' height='800' fill='url(%23g)'/%3E" +
               "%3Crect x='70' y='70' width='1060' height='660' rx='36' fill='white' fill-opacity='0.12'/%3E" +
               "%3Ctext x='600' y='390' text-anchor='middle' fill='white' " +
               "font-family='Arial, sans-serif' font-size='64' font-weight='700'%3EAduAja%3C/text%3E" +
               "%3Ctext x='600' y='460' text-anchor='middle' fill='white' " +
               "font-family='Arial, sans-serif' font-size='28' fill-opacity='0.9'%3EDummy Report Image%3C/text%3E" +
               "%3C/svg%3E";
    }

    // ============================================================
    // DRY: Session check — pola ini berulang di hampir semua method controller:
    //   String userId = (String) session.getAttribute("userId");
    //   if (userId == null) return "redirect:/warga/login";
    //
    // Gunakan: String userId = ControllerHelper.getSessionUserId(session);
    //          if (userId == null) return "redirect:/warga/login";
    // ============================================================
    public static String getSessionUserId(HttpSession session) {
        return (String) session.getAttribute("userId");
    }

    public static String getSessionUserName(HttpSession session) {
        return (String) session.getAttribute("userName");
    }

    public static String getSessionUserRole(HttpSession session) {
        return (String) session.getAttribute("userRole");
    }

    public static String requireRole(HttpSession session, String expectedRole) {
        String userId = getSessionUserId(session);
        String role   = getSessionUserRole(session);
        if (userId == null || role == null || !role.equals(expectedRole)) {
            if (session != null) session.invalidate();
            return null;
        }
        return userId;
    }

    public static String requireAnyAdminSession(HttpSession session) {
        String userId = getSessionUserId(session);
        String role   = getSessionUserRole(session);
        if (userId == null || role == null || !role.contains("ADMIN")) {
            if (session != null) session.invalidate();
            return null;
        }
        return userId;
    }

    public static String getSessionAgencyId(HttpSession session) {
        return (String) session.getAttribute(SESSION_AGENCY_ID);
    }

    public static String getSessionAgencyName(HttpSession session) {
        return (String) session.getAttribute(SESSION_AGENCY_NAME);
    }

    public static String getSessionRegionId(HttpSession session) {
        return (String) session.getAttribute(SESSION_REGION_ID);
    }

    public static String getSessionRegionName(HttpSession session) {
        return (String) session.getAttribute(SESSION_REGION_NAME);
    }

    public static String requireAgencySession(HttpSession session) {
        String userId = requireAnyAdminSession(session);
        String agencyId = getSessionAgencyId(session);
        if (userId == null || agencyId == null) return null;
        return userId;
    }

    public static String requirePusatSession(HttpSession session) {
        String userId = getSessionUserId(session);
        String role   = getSessionUserRole(session);
        String regionId = getSessionRegionId(session);
        if (userId == null || role == null || !role.equals("ADMIN_PUSAT")) {
            if (session != null) session.invalidate();
            return null;
        }
        return userId;
    }
}
