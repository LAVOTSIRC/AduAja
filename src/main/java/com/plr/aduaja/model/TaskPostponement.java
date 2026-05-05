package com.plr.aduaja.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "task_postponements")
public class TaskPostponement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "postponement_id")
    private String postponementId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private FieldTask task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by", nullable = false)
    private User requestedBy;

    @Column(name = "reason", columnDefinition = "TEXT", nullable = false)
    private String reason;

    @Column(name = "estimated_resume_at")
    private LocalDateTime estimatedResumeAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false)
    private ApprovalStatus approvalStatus = ApprovalStatus.MENUNGGU;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt = LocalDateTime.now();

    public enum ApprovalStatus {
        MENUNGGU, DISETUJUI, DITOLAK
    }

    public String getPostponementId() { return postponementId; }
    public void setPostponementId(String postponementId) { this.postponementId = postponementId; }

    public FieldTask getTask() { return task; }
    public void setTask(FieldTask task) { this.task = task; }

    public User getRequestedBy() { return requestedBy; }
    public void setRequestedBy(User requestedBy) { this.requestedBy = requestedBy; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDateTime getEstimatedResumeAt() { return estimatedResumeAt; }
    public void setEstimatedResumeAt(LocalDateTime estimatedResumeAt) { this.estimatedResumeAt = estimatedResumeAt; }

    public User getApprovedBy() { return approvedBy; }
    public void setApprovedBy(User approvedBy) { this.approvedBy = approvedBy; }

    public ApprovalStatus getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(ApprovalStatus approvalStatus) { this.approvalStatus = approvalStatus; }

    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }
}
