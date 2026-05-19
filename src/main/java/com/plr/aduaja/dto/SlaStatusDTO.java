package com.plr.aduaja.dto;

// ============================================================
// ENCAPSULATION — DTO memisahkan form input dari Entity langsung
// Field private + Getter & Setter publik
// ============================================================
public class SlaStatusDTO {

    private String reportId;          // PRIVATE — Enkapsulasi
    private String slaDeadlineAt;     // PRIVATE — Enkapsulasi
    private String currentStatus;     // PRIVATE — Enkapsulasi
    private Integer totalPausedMinutes; // PRIVATE — Enkapsulasi

    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }

    public String getSlaDeadlineAt() { return slaDeadlineAt; }
    public void setSlaDeadlineAt(String slaDeadlineAt) { this.slaDeadlineAt = slaDeadlineAt; }

    public String getCurrentStatus() { return currentStatus; }
    public void setCurrentStatus(String currentStatus) { this.currentStatus = currentStatus; }

    public Integer getTotalPausedMinutes() { return totalPausedMinutes; }
    public void setTotalPausedMinutes(Integer totalPausedMinutes) { this.totalPausedMinutes = totalPausedMinutes; }
}
