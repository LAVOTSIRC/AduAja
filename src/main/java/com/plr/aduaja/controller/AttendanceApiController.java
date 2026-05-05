package com.plr.aduaja.controller;

import com.plr.aduaja.model.Attendance;
import com.plr.aduaja.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceApiController {

    @Autowired
    private AttendanceService attendanceService;

    @GetMapping
    public ResponseEntity<List<Attendance>> getAllAttendance() {
        return ResponseEntity.ok(attendanceService.getAllAttendance());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Attendance>> getAttendanceByUser(@PathVariable String userId) {
        return ResponseEntity.ok(attendanceService.getAttendanceByUser(userId));
    }

    @GetMapping("/user/{userId}/active")
    public ResponseEntity<Attendance> getActiveAttendance(@PathVariable String userId) {
        Optional<Attendance> attendance = attendanceService.getActiveAttendance(userId);
        return attendance.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/checkin")
    public ResponseEntity<Attendance> checkIn(@RequestParam String userId,
                                               @RequestParam(required = false) String latitude,
                                               @RequestParam(required = false) String longitude,
                                               @RequestParam(required = false) String address,
                                               @RequestParam(required = false) String deviceInfo,
                                               @RequestParam(required = false) String ipAddress) {
        try {
            Attendance attendance = attendanceService.checkIn(userId, latitude, longitude, address, deviceInfo, ipAddress);
            return ResponseEntity.status(HttpStatus.CREATED).body(attendance);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/checkout/{attendanceId}")
    public ResponseEntity<Attendance> checkOut(@PathVariable String attendanceId,
                                                @RequestParam(required = false) String latitude,
                                                @RequestParam(required = false) String longitude,
                                                @RequestParam(required = false) String address) {
        try {
            return ResponseEntity.ok(attendanceService.checkOut(attendanceId, latitude, longitude, address));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}
