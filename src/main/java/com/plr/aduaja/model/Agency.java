package com.plr.aduaja.model;

import jakarta.persistence.*;

@Entity
@Table(name = "agencies")
public class Agency extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "agency_id")
    private String agencyId;

    @Column(name = "agency_name", nullable = false)
    private String agencyName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private Region region;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    public String getAgencyId() { return agencyId; }
    public void setAgencyId(String agencyId) { this.agencyId = agencyId; }

    public String getAgencyName() { return agencyName; }
    public void setAgencyName(String agencyName) { this.agencyName = agencyName; }

    public Region getRegion() { return region; }
    public void setRegion(Region region) { this.region = region; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
