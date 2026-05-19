package com.plr.aduaja.service;

import com.plr.aduaja.model.OfficerAttendance;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AttendanceService {

    List<OfficerAttendance> getAllAttendance();

    List<OfficerAttendance> getAttendanceByOfficer(String officerId);

    List<OfficerAttendance> getAttendanceByOfficerAndDateRange(String officerId, LocalDateTime start, LocalDateTime end);

    Optional<OfficerAttendance> getCurrentShift(String officerId);

    OfficerAttendance checkIn(String officerId, BigDecimal latitude, BigDecimal longitude, String deviceInfo);

    OfficerAttendance checkOut(String attendanceId);

    OfficerAttendance setBreak(String attendanceId);

    OfficerAttendance resumeFromBreak(String attendanceId);
}
