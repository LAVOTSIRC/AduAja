package com.plr.aduaja.dto;

// ============================================================
// ENCAPSULATION (Enkapsulasi): ResetPasswordDTO
// Memisahkan data form reset password dari Entity
// Berisi validasi kecocokan password baru
// Semua field PRIVATE — hanya bisa diakses melalui getter/setter
// ============================================================
public class ResetPasswordDTO {

    // ENKAPSULASI: semua field private
    private String email;
    private String otpCode;
    private String newPassword;
    private String confirmNewPassword;

    // ENKAPSULASI: Validation method — logika di dalam class
    public boolean isPasswordMatch() {
        return newPassword != null && newPassword.equals(confirmNewPassword);
    }

    public boolean isPasswordStrong() {
        return newPassword != null && newPassword.length() >= 8;
    }

    // ENKAPSULASI: hanya getter & setter
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getOtpCode() { return otpCode; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }

    public String getConfirmNewPassword() { return confirmNewPassword; }
    public void setConfirmNewPassword(String confirmNewPassword) { this.confirmNewPassword = confirmNewPassword; }
}
