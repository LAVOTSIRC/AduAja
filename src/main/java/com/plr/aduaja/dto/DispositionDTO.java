package com.plr.aduaja.dto;

import java.time.LocalDateTime;

public class DispositionDTO {

    private String reportId;
    private String dispatchedById;
    private String targetAgencyId;
    private String notes;
    private LocalDateTime dispatchedAt;

    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }

    public String getDispatchedById() { return dispatchedById; }
    public void setDispatchedById(String dispatchedById) { this.dispatchedById = dispatchedById; }

    public String getTargetAgencyId() { return targetAgencyId; }
    public void setTargetAgencyId(String targetAgencyId) { this.targetAgencyId = targetAgencyId; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getDispatchedAt() { return dispatchedAt; }
    public void setDispatchedAt(LocalDateTime dispatchedAt) { this.dispatchedAt = dispatchedAt; }
}
