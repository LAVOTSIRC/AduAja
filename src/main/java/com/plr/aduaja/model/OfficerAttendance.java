package com.plr.aduaja.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "officer_attendance")
public class OfficerAttendance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "attendance_id")
    private String attendanceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "officer_id", nullable = false)
    private User officer;

    @Column(name = "check_in_at", nullable = false)
    private LocalDateTime checkInAt;

    @Column(name = "check_in_latitude", precision = 10, scale = 8)
    private BigDecimal checkInLatitude;

    @Column(name = "check_in_longitude", precision = 11, scale = 8)
    private BigDecimal checkInLongitude;

    @Column(name = "check_out_at")
    private LocalDateTime checkOutAt;

    @Column(name = "device_info")
    private String deviceInfo;

    @Enumerated(EnumType.STRING)
    @Column(name = "shift_status", nullable = false)
    private ShiftStatus shiftStatus = ShiftStatus.AKTIF;

    public enum ShiftStatus {
        AKTIF, ISTIRAHAT, SELESAI_SHIFT
    }

    public String getAttendanceId() { return attendanceId; }
    public void setAttendanceId(String attendanceId) { this.attendanceId = attendanceId; }

    public User getOfficer() { return officer; }
    public void setOfficer(User officer) { this.officer = officer; }

    public LocalDateTime getCheckInAt() { return checkInAt; }
    public void setCheckInAt(LocalDateTime checkInAt) { this.checkInAt = checkInAt; }

    public BigDecimal getCheckInLatitude() { return checkInLatitude; }
    public void setCheckInLatitude(BigDecimal checkInLatitude) { this.checkInLatitude = checkInLatitude; }

    public BigDecimal getCheckInLongitude() { return checkInLongitude; }
    public void setCheckInLongitude(BigDecimal checkInLongitude) { this.checkInLongitude = checkInLongitude; }

    public LocalDateTime getCheckOutAt() { return checkOutAt; }
    public void setCheckOutAt(LocalDateTime checkOutAt) { this.checkOutAt = checkOutAt; }

    public String getDeviceInfo() { return deviceInfo; }
    public void setDeviceInfo(String deviceInfo) { this.deviceInfo = deviceInfo; }

    public ShiftStatus getShiftStatus() { return shiftStatus; }
    public void setShiftStatus(ShiftStatus shiftStatus) { this.shiftStatus = shiftStatus; }
}
