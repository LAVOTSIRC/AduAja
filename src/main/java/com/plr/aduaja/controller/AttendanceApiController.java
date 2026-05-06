package com.plr.aduaja.controller;

import com.plr.aduaja.model.OfficerAttendance;
import com.plr.aduaja.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceApiController {

    @Autowired
    private AttendanceService attendanceService;

    @GetMapping
    public ResponseEntity<List<OfficerAttendance>> getAllAttendance() {
        return ResponseEntity.ok(attendanceService.getAllAttendance());
    }

    @GetMapping("/officer/{officerId}")
    public ResponseEntity<List<OfficerAttendance>> getAttendanceByOfficer(@PathVariable String officerId) {
        return ResponseEntity.ok(attendanceService.getAttendanceByOfficer(officerId));
    }

    @GetMapping("/officer/{officerId}/current")
    public ResponseEntity<OfficerAttendance> getCurrentShift(@PathVariable String officerId) {
        Optional<OfficerAttendance> attendance = attendanceService.getCurrentShift(officerId);
        return attendance.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/checkin")
    public ResponseEntity<OfficerAttendance> checkIn(@RequestParam String officerId,
                                                      @RequestParam(required = false) BigDecimal latitude,
                                                      @RequestParam(required = false) BigDecimal longitude,
                                                      @RequestParam(required = false) String deviceInfo) {
        try {
            OfficerAttendance attendance = attendanceService.checkIn(officerId, latitude, longitude, deviceInfo);
            return ResponseEntity.status(HttpStatus.CREATED).body(attendance);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/checkout/{attendanceId}")
    public ResponseEntity<OfficerAttendance> checkOut(@PathVariable String attendanceId) {
        try {
            return ResponseEntity.ok(attendanceService.checkOut(attendanceId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/break/{attendanceId}")
    public ResponseEntity<OfficerAttendance> setBreak(@PathVariable String attendanceId) {
        try {
            return ResponseEntity.ok(attendanceService.setBreak(attendanceId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/resume/{attendanceId}")
    public ResponseEntity<OfficerAttendance> resumeFromBreak(@PathVariable String attendanceId) {
        try {
            return ResponseEntity.ok(attendanceService.resumeFromBreak(attendanceId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}
