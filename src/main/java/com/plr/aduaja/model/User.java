package com.plr.aduaja.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

// ============================================================
// INHERITANCE (Pewarisan): User extends BaseEntity
// → mewarisi createdAt & updatedAt secara otomatis
// ENKAPSULASI: semua field adalah PRIVATE
// ============================================================
@Entity
@Table(name = "users")
public class User extends BaseEntity {  // ← INHERITANCE sejati

    // ENKAPSULASI: field private
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")
    private String userId;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "phone_number", unique = true, length = 20)
    private String phoneNumber;

    // ENKAPSULASI: passwordHash tidak bisa diakses langsung
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false)
    private AccountStatus accountStatus = AccountStatus.PENDING;

    // HAS-A (Composition) ≠ Inheritance
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserProfile userProfile;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<OtpVerification> otpVerifications = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<ActiveSession> activeSessions = new ArrayList<>();

    @OneToMany(mappedBy = "reporter", cascade = CascadeType.ALL)
    private List<Report> reports = new ArrayList<>();

    // ============================================================
    // ENUM — Role & AccountStatus
    // ============================================================
    public enum Role {
        WARGA, ADMIN_PUSAT, ADMIN_DINAS, PETUGAS
    }

    public enum AccountStatus {
        PENDING, ACTIVE, SUSPENDED
    }

    // ============================================================
    // ENKAPSULASI: Getter & Setter untuk semua field PRIVATE
    // ============================================================
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public AccountStatus getAccountStatus() { return accountStatus; }
    public void setAccountStatus(AccountStatus accountStatus) { this.accountStatus = accountStatus; }

    public UserProfile getUserProfile() { return userProfile; }
    public void setUserProfile(UserProfile userProfile) { this.userProfile = userProfile; }

    public List<OtpVerification> getOtpVerifications() { return otpVerifications; }
    public void setOtpVerifications(List<OtpVerification> otpVerifications) { this.otpVerifications = otpVerifications; }

    public List<ActiveSession> getActiveSessions() { return activeSessions; }
    public void setActiveSessions(List<ActiveSession> activeSessions) { this.activeSessions = activeSessions; }

    public List<Report> getReports() { return reports; }
    public void setReports(List<Report> reports) { this.reports = reports; }
}
