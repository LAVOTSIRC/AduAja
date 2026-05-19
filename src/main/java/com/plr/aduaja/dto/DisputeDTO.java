package com.plr.aduaja.dto;

// ============================================================
// ENCAPSULATION — DTO memisahkan form input dari Entity langsung
// Field private + Getter & Setter publik
// ============================================================
public class DisputeDTO {

    private String reportId;         // PRIVATE — Enkapsulasi
    private String reason;           // PRIVATE — Enkapsulasi
    private String evidencePhotoUrl; // PRIVATE — Enkapsulasi
    private String adminDecision;    // PRIVATE — Enkapsulasi

    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getEvidencePhotoUrl() { return evidencePhotoUrl; }
    public void setEvidencePhotoUrl(String evidencePhotoUrl) { this.evidencePhotoUrl = evidencePhotoUrl; }

    public String getAdminDecision() { return adminDecision; }
    public void setAdminDecision(String adminDecision) { this.adminDecision = adminDecision; }
}
