package com.plr.aduaja.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance")
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;

    @Column(name = "check_out_time")
    private LocalDateTime checkOutTime;

    @Column(name = "check_in_latitude")
    private String checkInLatitude;

    @Column(name = "check_in_longitude")
    private String checkInLongitude;

    @Column(name = "check_in_address")
    private String checkInAddress;

    @Column(name = "check_out_latitude")
    private String checkOutLatitude;

    @Column(name = "check_out_longitude")
    private String checkOutLongitude;

    @Column(name = "check_out_address")
    private String checkOutAddress;

    @Column(name = "work_duration")
    private String workDuration;

    @Column(name = "device_info")
    private String deviceInfo;

    @Column(name = "ip_address")
    private String ipAddress;

    @Enumerated(EnumType.STRING)
    private Status status = Status.ONGOING;

    @Column(name = "date")
    private LocalDateTime date = LocalDateTime.now();

    public enum Status {
        ONGOING, COMPLETED
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public LocalDateTime getCheckInTime() { return checkInTime; }
    public void setCheckInTime(LocalDateTime checkInTime) { this.checkInTime = checkInTime; }

    public LocalDateTime getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(LocalDateTime checkOutTime) { this.checkOutTime = checkOutTime; }

    public String getCheckInLatitude() { return checkInLatitude; }
    public void setCheckInLatitude(String checkInLatitude) { this.checkInLatitude = checkInLatitude; }

    public String getCheckInLongitude() { return checkInLongitude; }
    public void setCheckInLongitude(String checkInLongitude) { this.checkInLongitude = checkInLongitude; }

    public String getCheckInAddress() { return checkInAddress; }
    public void setCheckInAddress(String checkInAddress) { this.checkInAddress = checkInAddress; }

    public String getCheckOutLatitude() { return checkOutLatitude; }
    public void setCheckOutLatitude(String checkOutLatitude) { this.checkOutLatitude = checkOutLatitude; }

    public String getCheckOutLongitude() { return checkOutLongitude; }
    public void setCheckOutLongitude(String checkOutLongitude) { this.checkOutLongitude = checkOutLongitude; }

    public String getCheckOutAddress() { return checkOutAddress; }
    public void setCheckOutAddress(String checkOutAddress) { this.checkOutAddress = checkOutAddress; }

    public String getWorkDuration() { return workDuration; }
    public void setWorkDuration(String workDuration) { this.workDuration = workDuration; }

    public String getDeviceInfo() { return deviceInfo; }
    public void setDeviceInfo(String deviceInfo) { this.deviceInfo = deviceInfo; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }
}
