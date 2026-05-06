package com.plr.aduaja.service;

import com.plr.aduaja.model.*;
import com.plr.aduaja.model.OfficerAttendance.ShiftStatus;
import com.plr.aduaja.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AttendanceService {

    @Autowired
    private OfficerAttendanceRepository attendanceRepository;

    @Autowired
    private UserRepository userRepository;

    public List<OfficerAttendance> getAllAttendance() {
        return attendanceRepository.findAll();
    }

    public List<OfficerAttendance> getAttendanceByOfficer(String officerId) {
        return attendanceRepository.findByOfficerUserId(officerId);
    }

    public Optional<OfficerAttendance> getCurrentShift(String officerId) {
        return attendanceRepository.findTopByOfficerUserIdAndShiftStatusNotOrderByCheckInAtDesc(
                officerId, ShiftStatus.SELESAI_SHIFT);
    }

    public OfficerAttendance checkIn(String officerId, BigDecimal latitude, BigDecimal longitude, String deviceInfo) {
        User officer = userRepository.findById(officerId)
                .orElseThrow(() -> new RuntimeException("Officer not found"));

        OfficerAttendance attendance = new OfficerAttendance();
        attendance.setOfficer(officer);
        attendance.setCheckInAt(LocalDateTime.now());
        attendance.setCheckInLatitude(latitude);
        attendance.setCheckInLongitude(longitude);
        attendance.setDeviceInfo(deviceInfo);
        attendance.setShiftStatus(ShiftStatus.AKTIF);

        return attendanceRepository.save(attendance);
    }

    public OfficerAttendance checkOut(String attendanceId) {
        OfficerAttendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new RuntimeException("Attendance record not found"));
        attendance.setCheckOutAt(LocalDateTime.now());
        attendance.setShiftStatus(ShiftStatus.SELESAI_SHIFT);
        return attendanceRepository.save(attendance);
    }

    public OfficerAttendance setBreak(String attendanceId) {
        OfficerAttendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new RuntimeException("Attendance record not found"));
        attendance.setShiftStatus(ShiftStatus.ISTIRAHAT);
        return attendanceRepository.save(attendance);
    }

    public OfficerAttendance resumeFromBreak(String attendanceId) {
        OfficerAttendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new RuntimeException("Attendance record not found"));
        attendance.setShiftStatus(ShiftStatus.AKTIF);
        return attendanceRepository.save(attendance);
    }
}
