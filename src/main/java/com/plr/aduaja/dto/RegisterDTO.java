package com.plr.aduaja.dto;

// ============================================================
// ENCAPSULATION (Enkapsulasi): RegisterDTO
// Memisahkan data form registrasi dari Entity User
// Berisi logika validasi (ENKAPSULASI: logika di dalam class)
// Semua field PRIVATE — hanya bisa diakses melalui getter/setter
// ============================================================
public class RegisterDTO {

    // ENKAPSULASI: semua field private
    private String fullName;
    private String email;
    private String phoneNumber;
    private String nik;
    private String password;
    private String confirmPassword;

    // ENKAPSULASI: Validation method — logika di dalam class
    public boolean isPasswordMatch() {
        return password != null && password.equals(confirmPassword);
    }

    public boolean isNikValid() {
        return nik != null && nik.length() == 16 && nik.matches("\\d+");
    }

    public boolean isPasswordStrong() {
        return password != null && password.length() >= 8;
    }

    // ENKAPSULASI: hanya getter & setter
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getNik() { return nik; }
    public void setNik(String nik) { this.nik = nik; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}
