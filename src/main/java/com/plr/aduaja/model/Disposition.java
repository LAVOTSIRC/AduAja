package com.plr.aduaja.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "dispositions")
public class Disposition {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "disposition_id")
    private String dispositionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispatched_by", nullable = false)
    private User dispatchedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_agency_id", nullable = false)
    private Agency targetAgency;

    @Column(name = "dispatched_at", nullable = false)
    private LocalDateTime dispatchedAt = LocalDateTime.now();

    @Column(columnDefinition = "TEXT")
    private String notes;

    public String getDispositionId() { return dispositionId; }
    public void setDispositionId(String dispositionId) { this.dispositionId = dispositionId; }

    public Report getReport() { return report; }
    public void setReport(Report report) { this.report = report; }

    public User getDispatchedBy() { return dispatchedBy; }
    public void setDispatchedBy(User dispatchedBy) { this.dispatchedBy = dispatchedBy; }

    public Agency getTargetAgency() { return targetAgency; }
    public void setTargetAgency(Agency targetAgency) { this.targetAgency = targetAgency; }

    public LocalDateTime getDispatchedAt() { return dispatchedAt; }
    public void setDispatchedAt(LocalDateTime dispatchedAt) { this.dispatchedAt = dispatchedAt; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
