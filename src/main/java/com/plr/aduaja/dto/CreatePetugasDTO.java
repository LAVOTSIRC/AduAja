package com.plr.aduaja.dto;

public class CreatePetugasDTO {

    private String fullName;
    private String email;
    private String phoneNumber;
    private String password;
    private String nip;
    private String wilayahTugasRegionId;
    private String agencyId;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNip() { return nip; }
    public void setNip(String nip) { this.nip = nip; }

    public String getWilayahTugasRegionId() { return wilayahTugasRegionId; }
    public void setWilayahTugasRegionId(String wilayahTugasRegionId) { this.wilayahTugasRegionId = wilayahTugasRegionId; }

    public String getAgencyId() { return agencyId; }
    public void setAgencyId(String agencyId) { this.agencyId = agencyId; }
}
