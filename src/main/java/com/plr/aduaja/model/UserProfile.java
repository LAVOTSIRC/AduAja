package com.plr.aduaja.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

// ============================================================
// INHERITANCE (Pewarisan): UserProfile extends BaseEntity
// HAS-A (Composition) dengan User ≠ Inheritance
// ENKAPSULASI: semua field adalah PRIVATE
// ============================================================
@Entity
@Table(name = "user_profiles")
public class UserProfile extends BaseEntity {  // ← INHERITANCE sejati

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "profile_id")
    private String profileId;

    // HAS-A (Composition) ≠ Inheritance
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(unique = true, length = 16)
    private String nik;

    @Column(name = "profile_photo_url")
    private String profilePhotoUrl;

    // HAS-A (Association) dengan Region
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domisili_region_id")
    private Region domisiliRegion;

    @Column(name = "nip", length = 50)
    private String nip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wilayah_tugas_region_id")
    private Region wilayahTugas;

    @Column(name = "alamat_lengkap", columnDefinition = "TEXT")
    private String alamatLengkap;

    @Column(name = "domisili_latitude", precision = 10, scale = 8)
    private BigDecimal domisiliLatitude;

    @Column(name = "domisili_longitude", precision = 11, scale = 8)
    private BigDecimal domisiliLongitude;

    // ENKAPSULASI: Getter & Setter untuk semua field PRIVATE
    public String getProfileId() { return profileId; }
    public void setProfileId(String profileId) { this.profileId = profileId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getNik() { return nik; }
    public void setNik(String nik) { this.nik = nik; }

    public String getProfilePhotoUrl() { return profilePhotoUrl; }
    public void setProfilePhotoUrl(String profilePhotoUrl) { this.profilePhotoUrl = profilePhotoUrl; }

    public Region getDomisiliRegion() { return domisiliRegion; }
    public void setDomisiliRegion(Region domisiliRegion) { this.domisiliRegion = domisiliRegion; }

    public String getNip() { return nip; }
    public void setNip(String nip) { this.nip = nip; }

    public Region getWilayahTugas() { return wilayahTugas; }
    public void setWilayahTugas(Region wilayahTugas) { this.wilayahTugas = wilayahTugas; }

    public String getAlamatLengkap() { return alamatLengkap; }
    public void setAlamatLengkap(String alamatLengkap) { this.alamatLengkap = alamatLengkap; }

    public BigDecimal getDomisiliLatitude() { return domisiliLatitude; }
    public void setDomisiliLatitude(BigDecimal domisiliLatitude) { this.domisiliLatitude = domisiliLatitude; }

    public BigDecimal getDomisiliLongitude() { return domisiliLongitude; }
    public void setDomisiliLongitude(BigDecimal domisiliLongitude) { this.domisiliLongitude = domisiliLongitude; }
}
