package com.plr.aduaja.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "report_revisions")
public class ReportRevision {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "revision_id")
    private String revisionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    @Column(name = "revised_photo_url")
    private String revisedPhotoUrl;

    @Column(name = "revised_description", columnDefinition = "TEXT")
    private String revisedDescription;

    @Column(name = "revised_latitude", precision = 10, scale = 8)
    private BigDecimal revisedLatitude;

    @Column(name = "revised_longitude", precision = 11, scale = 8)
    private BigDecimal revisedLongitude;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt = LocalDateTime.now();

    public String getRevisionId() { return revisionId; }
    public void setRevisionId(String revisionId) { this.revisionId = revisionId; }

    public Report getReport() { return report; }
    public void setReport(Report report) { this.report = report; }

    public String getRevisedPhotoUrl() { return revisedPhotoUrl; }
    public void setRevisedPhotoUrl(String revisedPhotoUrl) { this.revisedPhotoUrl = revisedPhotoUrl; }

    public String getRevisedDescription() { return revisedDescription; }
    public void setRevisedDescription(String revisedDescription) { this.revisedDescription = revisedDescription; }

    public BigDecimal getRevisedLatitude() { return revisedLatitude; }
    public void setRevisedLatitude(BigDecimal revisedLatitude) { this.revisedLatitude = revisedLatitude; }

    public BigDecimal getRevisedLongitude() { return revisedLongitude; }
    public void setRevisedLongitude(BigDecimal revisedLongitude) { this.revisedLongitude = revisedLongitude; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
}
