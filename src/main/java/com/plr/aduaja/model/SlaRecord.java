package com.plr.aduaja.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sla_records")
public class SlaRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "sla_id")
    private String slaId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false, unique = true)
    private Report report;

    @Column(name = "sla_start_at", nullable = false)
    private LocalDateTime slaStartAt;

    @Column(name = "sla_deadline_at", nullable = false)
    private LocalDateTime slaDeadlineAt;

    @Column(name = "total_paused_minutes", nullable = false)
    private Integer totalPausedMinutes = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_status", nullable = false)
    private SlaStatus currentStatus = SlaStatus.BERJALAN;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public enum SlaStatus {
        BERJALAN, TERTUNDA, TERLAMBAT, SELESAI
    }

    public String getSlaId() { return slaId; }
    public void setSlaId(String slaId) { this.slaId = slaId; }

    public Report getReport() { return report; }
    public void setReport(Report report) { this.report = report; }

    public LocalDateTime getSlaStartAt() { return slaStartAt; }
    public void setSlaStartAt(LocalDateTime slaStartAt) { this.slaStartAt = slaStartAt; }

    public LocalDateTime getSlaDeadlineAt() { return slaDeadlineAt; }
    public void setSlaDeadlineAt(LocalDateTime slaDeadlineAt) { this.slaDeadlineAt = slaDeadlineAt; }

    public Integer getTotalPausedMinutes() { return totalPausedMinutes; }
    public void setTotalPausedMinutes(Integer totalPausedMinutes) { this.totalPausedMinutes = totalPausedMinutes; }

    public SlaStatus getCurrentStatus() { return currentStatus; }
    public void setCurrentStatus(SlaStatus currentStatus) { this.currentStatus = currentStatus; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
