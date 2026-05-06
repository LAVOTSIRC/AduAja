package com.plr.aduaja.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sla_pause_logs")
public class SlaPauseLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "pause_log_id")
    private String pauseLogId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sla_id", nullable = false)
    private SlaRecord slaRecord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paused_by", nullable = false)
    private User pausedBy;

    @Column(name = "pause_reason", columnDefinition = "TEXT", nullable = false)
    private String pauseReason;

    @Column(name = "paused_at", nullable = false)
    private LocalDateTime pausedAt = LocalDateTime.now();

    @Column(name = "resumed_at")
    private LocalDateTime resumedAt;

    @Column(name = "paused_duration_minutes")
    private Integer pausedDurationMinutes;

    public String getPauseLogId() { return pauseLogId; }
    public void setPauseLogId(String pauseLogId) { this.pauseLogId = pauseLogId; }

    public SlaRecord getSlaRecord() { return slaRecord; }
    public void setSlaRecord(SlaRecord slaRecord) { this.slaRecord = slaRecord; }

    public User getPausedBy() { return pausedBy; }
    public void setPausedBy(User pausedBy) { this.pausedBy = pausedBy; }

    public String getPauseReason() { return pauseReason; }
    public void setPauseReason(String pauseReason) { this.pauseReason = pauseReason; }

    public LocalDateTime getPausedAt() { return pausedAt; }
    public void setPausedAt(LocalDateTime pausedAt) { this.pausedAt = pausedAt; }

    public LocalDateTime getResumedAt() { return resumedAt; }
    public void setResumedAt(LocalDateTime resumedAt) { this.resumedAt = resumedAt; }

    public Integer getPausedDurationMinutes() { return pausedDurationMinutes; }
    public void setPausedDurationMinutes(Integer pausedDurationMinutes) { this.pausedDurationMinutes = pausedDurationMinutes; }
}
