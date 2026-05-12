package com.plr.aduaja.dto;

// ============================================================
// ENCAPSULATION (Enkapsulasi): ProfileDTO
// Memisahkan data form edit profil dari Entity User & UserProfile
// Semua field PRIVATE — hanya bisa diakses melalui getter/setter
// ============================================================
public class ProfileDTO {

    // ENKAPSULASI: semua field private
    private String fullName;
    private String email;
    private String phoneNumber;
    private String nik;
    private String alamatLengkap;
    private String profilePhotoUrl;

    // ENKAPSULASI: hanya getter & setter
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getNik() { return nik; }
    public void setNik(String nik) { this.nik = nik; }

    public String getAlamatLengkap() { return alamatLengkap; }
    public void setAlamatLengkap(String alamatLengkap) { this.alamatLengkap = alamatLengkap; }

    public String getProfilePhotoUrl() { return profilePhotoUrl; }
    public void setProfilePhotoUrl(String profilePhotoUrl) { this.profilePhotoUrl = profilePhotoUrl; }
}
