package com.plr.aduaja.service;

import com.plr.aduaja.model.User;
import com.plr.aduaja.dto.LoginDTO;

import java.util.Optional;

// ============================================================
// ABSTRACTION (Abstraksi): AuthService Interface sebagai kontrak
// Controller hanya tahu interface ini, tidak tahu implementasinya
//
// POLYMORPHISM (Compile-time / Overloading):
// - login(LoginDTO dto, String ip)
// - loginByEmail(String email, String password, String ip)  ← Overload
// - loginByPhone(String phone, String password, String ip)  ← Overload
// ============================================================
public interface AuthService {

    // ===========================
    // OVERLOADING (Compile-time Polymorphism)
    // Login dengan cara berbeda: DTO, email langsung, atau nomor HP
    // ===========================
    Optional<User> login(LoginDTO dto, String ipAddress);
    Optional<User> loginByEmail(String email, String password, String ipAddress);  // ← Overload
    Optional<User> loginByPhone(String phone, String password, String ipAddress);  // ← Overload

    // ===========================
    // METHOD KEAMANAN
    // ===========================
    boolean verifyPassword(String rawPassword, String hashedPassword);
    boolean isAccountLocked(String email);
    void recordLoginAttempt(String email, boolean success, String ipAddress);
    void logout(String userId);
}
