package com.plr.aduaja.dto;

// ============================================================
// ENCAPSULATION (Enkapsulasi): LoginDTO
// Memisahkan data form login dari Entity User
// Semua field PRIVATE — hanya bisa diakses melalui getter/setter
// ============================================================
public class LoginDTO {

    // ENKAPSULASI: field private
    private String email;
    private String password;

    // ENKAPSULASI: hanya getter & setter
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
