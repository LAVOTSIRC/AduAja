package com.plr.aduaja.dto;

// ============================================================
// ENCAPSULATION — DTO memisahkan form input dari Entity langsung
// Field private + Getter & Setter publik
// ============================================================
public class MergeDTO {

    private String primaryReportId;  // PRIVATE — Enkapsulasi
    private String mergedReportId;   // PRIVATE — Enkapsulasi
    private String reason;           // PRIVATE — Enkapsulasi
    private Integer similarityScore; // PRIVATE — Enkapsulasi

    public String getPrimaryReportId() { return primaryReportId; }
    public void setPrimaryReportId(String primaryReportId) { this.primaryReportId = primaryReportId; }

    public String getMergedReportId() { return mergedReportId; }
    public void setMergedReportId(String mergedReportId) { this.mergedReportId = mergedReportId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Integer getSimilarityScore() { return similarityScore; }
    public void setSimilarityScore(Integer similarityScore) { this.similarityScore = similarityScore; }
}
