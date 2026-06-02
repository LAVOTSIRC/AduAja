package com.plr.aduaja.dto;

// ============================================================
// ENCAPSULATION (Enkapsulasi): OtpVerifyDTO
// Memisahkan data form verifikasi OTP dari Entity
// Semua field PRIVATE — hanya bisa diakses melalui getter/setter
// ============================================================
public class OtpVerifyDTO {

    // ENKAPSULASI: semua field private
    private String userId;
    private String email;
    private String otpCode;

    // ENKAPSULASI: hanya getter & setter
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getOtpCode() { return otpCode; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }
}
