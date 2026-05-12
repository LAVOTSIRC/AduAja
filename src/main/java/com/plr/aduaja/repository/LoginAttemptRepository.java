package com.plr.aduaja.repository;

import com.plr.aduaja.model.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

// ============================================================
// Repository untuk keamanan — track login attempts
// Digunakan oleh AuthServiceImpl untuk lockout mechanism
// ============================================================
@Repository
public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, String> {

    // Method untuk menghitung percobaan login gagal berdasarkan email
    // Digunakan oleh AuthServiceImpl.isAccountLocked()
    long countByEmailAndIsSuccessAndAttemptedAtAfter(String email, Boolean isSuccess, LocalDateTime after);

    // Mendapatkan riwayat login attempt berdasarkan email
    List<LoginAttempt> findByEmailOrderByAttemptedAtDesc(String email);

    // Menghitung failed attempts berdasarkan IP address (untuk brute force protection)
    long countByIpAddressAndIsSuccessFalseAndAttemptedAtAfter(String ipAddress, LocalDateTime after);
}
