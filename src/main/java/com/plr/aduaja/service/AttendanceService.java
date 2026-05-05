package com.plr.aduaja.service;

import com.plr.aduaja.model.Attendance;
import com.plr.aduaja.model.User;
import com.plr.aduaja.repository.AttendanceRepository;
import com.plr.aduaja.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Service
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Attendance> getAllAttendance() {
        return attendanceRepository.findAll();
    }

    public List<Attendance> getAttendanceByUser(String userId) {
        return attendanceRepository.findByUserIdOrderByDateDesc(userId);
    }

    public Optional<Attendance> getActiveAttendance(String userId) {
        return attendanceRepository.findByUserIdAndStatus(userId, Attendance.Status.ONGOING);
    }

    public Attendance checkIn(String userId, String latitude, String longitude, String address, String deviceInfo, String ipAddress) {
        Optional<Attendance> existing = attendanceRepository.findByUserIdAndStatus(userId, Attendance.Status.ONGOING);
        if (existing.isPresent()) {
            throw new RuntimeException("User already checked in");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Attendance attendance = new Attendance();
        attendance.setUser(user);
        attendance.setCheckInTime(LocalDateTime.now());
        attendance.setCheckInLatitude(latitude);
        attendance.setCheckInLongitude(longitude);
        attendance.setCheckInAddress(address);
        attendance.setDeviceInfo(deviceInfo);
        attendance.setIpAddress(ipAddress);
        attendance.setDate(LocalDateTime.now());
        attendance.setStatus(Attendance.Status.ONGOING);

        return attendanceRepository.save(attendance);
    }

    public Attendance checkOut(String attendanceId, String latitude, String longitude, String address) {
        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new RuntimeException("Attendance not found"));

        attendance.setCheckOutTime(LocalDateTime.now());
        attendance.setCheckOutLatitude(latitude);
        attendance.setCheckOutLongitude(longitude);
        attendance.setCheckOutAddress(address);
        attendance.setStatus(Attendance.Status.COMPLETED);

        Duration duration = Duration.between(attendance.getCheckInTime(), attendance.getCheckOutTime());
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        attendance.setWorkDuration(String.format("%02d:%02d:%02d", hours, minutes, seconds));

        return attendanceRepository.save(attendance);
    }

    public long countByUser(String userId) {
        return attendanceRepository.countByUserId(userId);
    }
}
