package com.plr.aduaja.controller;

import lombok.extern.slf4j.Slf4j;
import com.plr.aduaja.model.*;
import com.plr.aduaja.model.FieldTask.TaskStatus;
import com.plr.aduaja.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Controller
public class PetugasController {

    @Autowired
    private FieldTaskService fieldTaskService;

    @Autowired
    private UserService userService;

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/petugas/home")
    public String petugasHome() {
        return "redirect:/petugas/dashboard";
    }

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
        } catch (Exception e) {
            log.error("Gagal proses absensi petugas: {}", e.getMessage(), e);
        }

        return "redirect:/petugas/dashboard";
    }

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
                switch (action) {
                    case "start" -> fieldTaskService.startTask(id, latitude, longitude);
                    case "complete" -> fieldTaskService.completeTask(id);
                    case "postpone" -> {
                        String reason = description != null && !description.isBlank() ? description : "Ditunda oleh petugas";
                        fieldTaskService.postponeTask(id, reason);
                    }
                    case "reassign" -> {
                        String targetOfficer = newOfficerId != null ? newOfficerId : "";
                        if (!targetOfficer.isBlank()) fieldTaskService.reassignTask(id, targetOfficer);
                    }
                }
            } catch (Exception e) {
                log.error("Gagal aksi tugas {} - {}: {}", id, action, e.getMessage(), e);
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
        // SESSION CHECK — harus login sebelum akses dashboard
        String userId = ControllerHelper.getSessionUserId(session);
        if (userId == null) return "redirect:/petugas/login";

        // Isi data dari DB nyata — tidak ada hardcoded dummy data
        userService.findById(userId).ifPresentOrElse(officer -> {
            model.addAttribute("user", Map.of(
                "name", officer.getFullName(),
                "dinas", "Dinas Pekerjaan Umum"
            ));
        }, () -> {
            // User tidak ditemukan di DB — invalidate session
            session.invalidate();
        });

        if (!model.containsAttribute("user")) return "redirect:/petugas/login";

        List<FieldTask> realTasks = fieldTaskService.getTasksByOfficer(userId);
        long s  = realTasks.stream().filter(t -> t.getTaskStatus() == TaskStatus.SELESAI).count();
        long ip = realTasks.stream().filter(t -> t.getTaskStatus() == TaskStatus.SEDANG_DIKERJAKAN).count();
        long n  = realTasks.stream().filter(t -> t.getTaskStatus() == TaskStatus.BARU).count();
        long p  = realTasks.stream().filter(t -> t.getTaskStatus() == TaskStatus.TERTUNDA).count();
        model.addAttribute("stats", Map.of(
            "selesaiHariIni", s, "sedangDikerjakan", ip, "tugasBaru", n, "tertunda", p));

        List<Map<String, Object>> activeTasks = realTasks.stream()
            .limit(5).map(this::toPetugasTaskMap).collect(Collectors.toList());
        model.addAttribute("activeTasks", activeTasks);

        // Absensi dari DB
        Map<String, Object> attendance = new HashMap<>();
        attendance.put("attendanceId", "-");
        attendance.put("checkedIn", false);
        attendance.put("currentStatus", "Belum Check-In");
        attendance.put("checkInTime", "-");
        attendance.put("workDuration", "00:00:00");
        attendance.put("location", "-");
        attendanceService.getCurrentShift(userId).ifPresent(shift -> {
            attendance.put("attendanceId", shift.getAttendanceId());
            attendance.put("checkedIn", shift.getCheckInAt() != null);
            attendance.put("currentStatus", shift.getShiftStatus() == OfficerAttendance.ShiftStatus.AKTIF ? "Siap Bertugas"
                : shift.getShiftStatus() == OfficerAttendance.ShiftStatus.ISTIRAHAT ? "Istirahat" : "Selesai Shift");
            attendance.put("checkInTime", shift.getCheckInAt() != null
                ? shift.getCheckInAt().format(ControllerHelper.TIME_FMT) : "-");
            attendance.put("workDuration", shift.getCheckInAt() != null
                ? formatDuration(Duration.between(shift.getCheckInAt(), LocalDateTime.now())) : "00:00:00");
            attendance.put("location", shift.getCheckInLatitude() != null
                ? shift.getCheckInLatitude() + ", " + shift.getCheckInLongitude() : "-");
        });
        model.addAttribute("attendance", attendance);
        model.addAttribute("deviceInfo", Map.of("browser", "-", "os", "-"));
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
                    ft.getCreatedAt() != null ? ft.getCreatedAt().format(ControllerHelper.DATETIME_FMT) : "-",
                    "note", "Tugas diterima dari laporan warga"));
                if (ft.getStartedAt() != null) {
                    statusHistory.add(Map.of("status", "Mulai Dikerjakan", "time",
                        ft.getStartedAt().format(ControllerHelper.DATETIME_FMT),
                        "note", "Petugas memulai pengerjaan"));
                }
                if (ft.getCompletedAt() != null) {
                    statusHistory.add(Map.of("status", "Selesai", "time",
                        ft.getCompletedAt().format(ControllerHelper.DATETIME_FMT),
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
            } catch (Exception e) {
                log.error("Gagal complete tugas {}: {}", id, e.getMessage(), e);
            }
            return "redirect:/petugas/dashboard";
        }
        return "redirect:/petugas/task-execution?id=" + id;
    }

    @GetMapping("/petugas/history")
    public String petugasHistory(Model model, HttpSession session) {
        // SESSION CHECK
        String userId = ControllerHelper.getSessionUserId(session);
        if (userId == null) return "redirect:/petugas/login";

        // Selalu dari DB — TIDAK ada fallback ke data dummy hardcoded
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

        // DRY: gunakan DATE_FMT dari ControllerHelper
        List<Map<String, Object>> taskList = completed.stream().map(t -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", t.getTaskId());
            m.put("title", t.getReport() != null ? "Tugas #" + t.getTaskId().substring(0, 8) : "-");
            m.put("category", t.getReport() != null && t.getReport().getCategory() != null
                ? t.getReport().getCategory().getCategoryName() : "Lainnya");
            m.put("location", t.getReport() != null && t.getReport().getLocationHint() != null
                ? t.getReport().getLocationHint() : "-");
            m.put("status", "approved");
            m.put("duration", t.getStartedAt() != null && t.getCompletedAt() != null
                ? formatDuration(Duration.between(t.getStartedAt(), t.getCompletedAt())) : "-");
            m.put("completedAt", t.getCompletedAt() != null
                ? t.getCompletedAt().format(ControllerHelper.DATE_FMT) : "-");
            m.put("photoBefore", 0);
            m.put("photoAfter", 0);
            m.put("materialUsed", 0);
            return m;
        }).collect(Collectors.toList());

        model.addAttribute("tasks", taskList);
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
        // SESSION CHECK
        String userId = ControllerHelper.getSessionUserId(session);
        if (userId == null) return "redirect:/petugas/login";
        {
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
                    // DRY: gunakan konstanta dari ControllerHelper
                    String dateFmt = r.getCheckInAt() != null ? r.getCheckInAt().format(ControllerHelper.DATE_FMT) : "-";
                    String ciTime = r.getCheckInAt() != null ? r.getCheckInAt().format(ControllerHelper.TIME_FMT) : "-";
                    String coTime = r.getCheckOutAt() != null ? r.getCheckOutAt().format(ControllerHelper.TIME_FMT) : null;
                    String dur = r.getCheckInAt() != null && r.getCheckOutAt() != null
                        ? formatDuration(Duration.between(r.getCheckInAt(), r.getCheckOutAt())) : "-";
                    String status = r.getShiftStatus() == OfficerAttendance.ShiftStatus.SELESAI_SHIFT ? "completed" : "ongoing";
                    return buildAttendanceRecord(dateFmt, ciTime, coTime, dur, status,
                        Map.of("address", r.getCheckInLatitude() != null
                            ? r.getCheckInLatitude().toPlainString() + ", " + r.getCheckInLongitude().toPlainString() : "Kantor Dinas"),
                        r.getCheckOutAt() != null ? Map.of("address", "Check-out lokasi") : null,
                        di);
                }).collect(Collectors.toList());
                model.addAttribute("user", Map.of("name", "Petugas"));
                model.addAttribute("summary", Map.of("daysPresent", (int) daysPresent, "totalHours", totalMinutes / 60, "lateCount", (int) lateCount));
                model.addAttribute("attendanceRecords", recordList);
                return "petugas/attendance-history";
            }
        }

        // Jika tidak ada riwayat di DB — tampilkan list kosong (jangan hardcode)
        model.addAttribute("user", Map.of("name", "-"));
        model.addAttribute("summary", Map.of("daysPresent", 0, "totalHours", 0L, "lateCount", 0));
        model.addAttribute("attendanceRecords", new ArrayList<>());
        return "petugas/attendance-history";
    }

    // ==========================================
    // PRIVATE HELPERS
    // ==========================================

    private String formatDuration(Duration d) {
        long hours = d.toHours();
        long mins = d.toMinutes() % 60;
        long secs = d.getSeconds() % 60;
        return String.format("%02d:%02d:%02d", hours, mins, secs);
    }

    private Map<String, Object> toPetugasTaskMap(FieldTask task) {
        // ENKAPSULASI: private helper — detail konversi disembunyikan dari luar
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
        // DRY: gunakan konstanta DATETIME_FMT dari ControllerHelper
        m.put("reportDate", task.getReport() != null && task.getReport().getSubmittedAt() != null
            ? task.getReport().getSubmittedAt().format(ControllerHelper.DATE_FMT) : "-");
        m.put("slaDeadline", "-");
        m.put("slaStatusText", "-");
        m.put("slaStatusClass", "text-gray-600");
        m.put("distanceToTask", "-");
        // DRY: gunakan konstanta DATETIME_FMT dari ControllerHelper
        if (task.getStartedAt() != null) m.put("startedAt", task.getStartedAt().format(ControllerHelper.DATETIME_FMT));
        m.put("officerLatitude", task.getOfficerLatitude());
        m.put("officerLongitude", task.getOfficerLongitude());
        return m;
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
}
