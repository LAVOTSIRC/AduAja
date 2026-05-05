package com.plr.aduaja.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String ticketNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id")
    private Report report;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_petugas_id")
    private User assignedPetugas;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by_dinas_id")
    private User assignedByDinas;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.BARU;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    @Column(name = "location", nullable = false)
    private String location;

    @Column(name = "latitude")
    private String latitude;

    @Column(name = "longitude")
    private String longitude;

    @Column(name = "distance_to_task")
    private String distanceToTask;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TicketProgress> progressHistory = new ArrayList<>();

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL)
    private List<PhotoEvidence> photoEvidence = new ArrayList<>();

    @Column(name = "sla_deadline")
    private LocalDateTime slaDeadline;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "pending_reason")
    private String pendingReason;

    @Column(name = "completion_notes")
    private String completionNotes;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL)
    private List<MaterialUsage> materials = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_ticket_id")
    private Ticket parentTicket;

    @OneToMany(mappedBy = "parentTicket")
    private List<Ticket> childTickets = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Status {
        BARU, IN_PROGRESS, PENDING, SELESAI, DIBATALKAN, ESKALASI
    }

    public enum Priority {
        RENDAH, SEDANG, TINGGI, KRITIS
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTicketNumber() { return ticketNumber; }
    public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }

    public Report getReport() { return report; }
    public void setReport(Report report) { this.report = report; }

    public User getAssignedPetugas() { return assignedPetugas; }
    public void setAssignedPetugas(User assignedPetugas) { this.assignedPetugas = assignedPetugas; }

    public User getAssignedByDinas() { return assignedByDinas; }
    public void setAssignedByDinas(User assignedByDinas) { this.assignedByDinas = assignedByDinas; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getLatitude() { return latitude; }
    public void setLatitude(String latitude) { this.latitude = latitude; }

    public String getLongitude() { return longitude; }
    public void setLongitude(String longitude) { this.longitude = longitude; }

    public String getDistanceToTask() { return distanceToTask; }
    public void setDistanceToTask(String distanceToTask) { this.distanceToTask = distanceToTask; }

    public List<TicketProgress> getProgressHistory() { return progressHistory; }
    public void setProgressHistory(List<TicketProgress> progressHistory) { this.progressHistory = progressHistory; }

    public List<PhotoEvidence> getPhotoEvidence() { return photoEvidence; }
    public void setPhotoEvidence(List<PhotoEvidence> photoEvidence) { this.photoEvidence = photoEvidence; }

    public LocalDateTime getSlaDeadline() { return slaDeadline; }
    public void setSlaDeadline(LocalDateTime slaDeadline) { this.slaDeadline = slaDeadline; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public String getPendingReason() { return pendingReason; }
    public void setPendingReason(String pendingReason) { this.pendingReason = pendingReason; }

    public String getCompletionNotes() { return completionNotes; }
    public void setCompletionNotes(String completionNotes) { this.completionNotes = completionNotes; }

    public List<MaterialUsage> getMaterials() { return materials; }
    public void setMaterials(List<MaterialUsage> materials) { this.materials = materials; }

    public Ticket getParentTicket() { return parentTicket; }
    public void setParentTicket(Ticket parentTicket) { this.parentTicket = parentTicket; }

    public List<Ticket> getChildTickets() { return childTickets; }
    public void setChildTickets(List<Ticket> childTickets) { this.childTickets = childTickets; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
