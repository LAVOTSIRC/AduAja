package com.plr.aduaja.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// ============================================================
// INHERITANCE (Pewarisan): LoginAttempt extends BaseEntity
// Mencatat percobaan login (sukses & gagal) untuk keamanan
// ENKAPSULASI: semua field adalah PRIVATE
// ============================================================
@Entity
@Table(name = "login_attempts")
public class LoginAttempt extends BaseEntity {  // ← INHERITANCE sejati

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "attempt_id")
    private String attemptId;

    // email yang digunakan saat login (bisa tidak ada usernya jika gagal)
    @Column(nullable = false, length = 150)
    private String email;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "is_success", nullable = false)
    private Boolean isSuccess = false;

    @Column(name = "attempted_at", nullable = false)
    private LocalDateTime attemptedAt = LocalDateTime.now();

    // ENKAPSULASI: Getter & Setter untuk semua field PRIVATE
    public String getAttemptId() { return attemptId; }
    public void setAttemptId(String attemptId) { this.attemptId = attemptId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public Boolean getIsSuccess() { return isSuccess; }
    public void setIsSuccess(Boolean isSuccess) { this.isSuccess = isSuccess; }

    public LocalDateTime getAttemptedAt() { return attemptedAt; }
    public void setAttemptedAt(LocalDateTime attemptedAt) { this.attemptedAt = attemptedAt; }
}
