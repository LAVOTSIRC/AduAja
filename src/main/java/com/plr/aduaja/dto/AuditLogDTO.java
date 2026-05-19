package com.plr.aduaja.dto;

// ============================================================
// ENCAPSULATION — DTO memisahkan form input dari Entity langsung
// Field private + Getter & Setter publik
// ============================================================
public class AuditLogDTO {

    private String actionType;  // PRIVATE — Enkapsulasi
    private String reportId;    // PRIVATE — Enkapsulasi
    private String startDate;   // PRIVATE — Enkapsulasi
    private String endDate;     // PRIVATE — Enkapsulasi

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
}
