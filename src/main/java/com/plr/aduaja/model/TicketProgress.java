package com.plr.aduaja.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_progress")
public class TicketProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by", nullable = false)
    private User updatedBy;

    @Column(name = "status_from")
    private String statusFrom;

    @Column(name = "status_to")
    private String statusTo;

    @Column(name = "keterangan", columnDefinition = "TEXT")
    private String keterangan;

    @Column(name = "estimasi_selesai")
    private String estimasiSelesai;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Ticket getTicket() { return ticket; }
    public void setTicket(Ticket ticket) { this.ticket = ticket; }

    public User getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(User updatedBy) { this.updatedBy = updatedBy; }

    public String getStatusFrom() { return statusFrom; }
    public void setStatusFrom(String statusFrom) { this.statusFrom = statusFrom; }

    public String getStatusTo() { return statusTo; }
    public void setStatusTo(String statusTo) { this.statusTo = statusTo; }

    public String getKeterangan() { return keterangan; }
    public void setKeterangan(String keterangan) { this.keterangan = keterangan; }

    public String getEstimasiSelesai() { return estimasiSelesai; }
    public void setEstimasiSelesai(String estimasiSelesai) { this.estimasiSelesai = estimasiSelesai; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
