package com.plr.aduaja.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sengketa")
public class Sengketa {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "sengketa_number", nullable = false, unique = true)
    private String sengketaNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "filed_by", nullable = false)
    private User filedBy;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String alasan;

    @Column(name = "foto_bukti_path")
    private String fotoBuktiPath;

    @Column(name = "keterangan_dinas", columnDefinition = "TEXT")
    private String keteranganDinas;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.MENUNGGU_TINJAUAN;

    @Column(name = "keputusan_admin")
    private String keputusanAdmin;

    @Column(name = "catatan_admin", columnDefinition = "TEXT")
    private String catatanAdmin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by")
    private User resolvedBy;

    @Enumerated(EnumType.STRING)
    private Priority prioritas;

    @Column(name = "filed_at")
    private LocalDateTime filedAt = LocalDateTime.now();

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    public enum Status {
        MENUNGGU_TINJAUAN, DIPROSES, DISETUJUI, DITOLAK
    }

    public enum Priority {
        RENDAH, SEDANG, TINGGI, KRITIS
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSengketaNumber() { return sengketaNumber; }
    public void setSengketaNumber(String sengketaNumber) { this.sengketaNumber = sengketaNumber; }

    public Ticket getTicket() { return ticket; }
    public void setTicket(Ticket ticket) { this.ticket = ticket; }

    public Report getReport() { return report; }
    public void setReport(Report report) { this.report = report; }

    public User getFiledBy() { return filedBy; }
    public void setFiledBy(User filedBy) { this.filedBy = filedBy; }

    public String getAlasan() { return alasan; }
    public void setAlasan(String alasan) { this.alasan = alasan; }

    public String getFotoBuktiPath() { return fotoBuktiPath; }
    public void setFotoBuktiPath(String fotoBuktiPath) { this.fotoBuktiPath = fotoBuktiPath; }

    public String getKeteranganDinas() { return keteranganDinas; }
    public void setKeteranganDinas(String keteranganDinas) { this.keteranganDinas = keteranganDinas; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public String getKeputusanAdmin() { return keputusanAdmin; }
    public void setKeputusanAdmin(String keputusanAdmin) { this.keputusanAdmin = keputusanAdmin; }

    public String getCatatanAdmin() { return catatanAdmin; }
    public void setCatatanAdmin(String catatanAdmin) { this.catatanAdmin = catatanAdmin; }

    public User getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(User resolvedBy) { this.resolvedBy = resolvedBy; }

    public Priority getPrioritas() { return prioritas; }
    public void setPrioritas(Priority prioritas) { this.prioritas = prioritas; }

    public LocalDateTime getFiledAt() { return filedAt; }
    public void setFiledAt(LocalDateTime filedAt) { this.filedAt = filedAt; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
}
