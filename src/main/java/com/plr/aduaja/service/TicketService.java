package com.plr.aduaja.service;

import com.plr.aduaja.model.*;
import com.plr.aduaja.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketProgressRepository progressRepository;

    @Autowired
    private MaterialUsageRepository materialRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReportService reportService;

    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    public List<Ticket> getTicketsByStatus(Ticket.Status status) {
        return ticketRepository.findByStatusOrderBySlaDeadlineAsc(status);
    }

    public List<Ticket> getTicketsByPetugas(String petugasId) {
        return ticketRepository.findByPetugasOrderByCreatedAtDesc(petugasId);
    }

    public List<Ticket> getActiveTicketsByPetugas(String petugasId) {
        return ticketRepository.findByAssignedPetugasIdAndStatus(petugasId, Ticket.Status.IN_PROGRESS);
    }

    public Optional<Ticket> getTicketById(String id) {
        return ticketRepository.findById(id);
    }

    public Optional<Ticket> getTicketByNumber(String ticketNumber) {
        return ticketRepository.findByTicketNumber(ticketNumber);
    }

    public Ticket createTicket(Report report, User petugas, User assignedBy) {
        Ticket ticket = new Ticket();
        ticket.setTicketNumber("TKT-" + System.currentTimeMillis());
        ticket.setReport(report);
        ticket.setAssignedPetugas(petugas);
        ticket.setAssignedByDinas(assignedBy);
        ticket.setStatus(Ticket.Status.BARU);
        ticket.setLocation(report.getLocation());
        ticket.setLatitude(report.getLatitude());
        ticket.setLongitude(report.getLongitude());
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setSlaDeadline(LocalDateTime.now().plusDays(3));

        Ticket saved = ticketRepository.save(ticket);

        reportService.updateStatus(report.getId(), Report.Status.DIPROSES);
        return saved;
    }

    public Ticket startTicket(String ticketId, String petugasId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        ticket.setStatus(Ticket.Status.IN_PROGRESS);
        ticket.setStartedAt(LocalDateTime.now());

        TicketProgress progress = new TicketProgress();
        progress.setTicket(ticket);
        progress.setUpdatedBy(userRepository.findById(petugasId).orElse(null));
        progress.setStatusFrom("BARU");
        progress.setStatusTo("IN_PROGRESS");
        progress.setKeterangan("Petugas memulai pengerjaan tugas");
        progress.setCreatedAt(LocalDateTime.now());
        progressRepository.save(progress);

        return ticketRepository.save(ticket);
    }

    public Ticket completeTicket(String ticketId, String completionNotes) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        ticket.setStatus(Ticket.Status.SELESAI);
        ticket.setCompletedAt(LocalDateTime.now());
        ticket.setCompletionNotes(completionNotes);

        return ticketRepository.save(ticket);
    }

    public Ticket pendingTicket(String ticketId, String reason) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        ticket.setStatus(Ticket.Status.PENDING);
        ticket.setPendingReason(reason);

        return ticketRepository.save(ticket);
    }

    public Ticket updateStatus(String ticketId, Ticket.Status status) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        ticket.setStatus(status);
        return ticketRepository.save(ticket);
    }

    public Ticket pauseTicket(String ticketId, String reason) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        ticket.setStatus(Ticket.Status.PENDING);
        ticket.setPendingReason(reason);
        return ticketRepository.save(ticket);
    }

    public Ticket resumeTicket(String ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        ticket.setStatus(Ticket.Status.IN_PROGRESS);
        ticket.setPendingReason(null);
        return ticketRepository.save(ticket);
    }

    public Ticket addProgress(String ticketId, String petugasId, String keterangan, String estimasi) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        TicketProgress progress = new TicketProgress();
        progress.setTicket(ticket);
        progress.setUpdatedBy(userRepository.findById(petugasId).orElse(null));
        progress.setKeterangan(keterangan);
        progress.setEstimasiSelesai(estimasi);
        progress.setCreatedAt(LocalDateTime.now());
        progressRepository.save(progress);

        return ticket;
    }

    public Ticket addMaterial(String ticketId, String materialName, Integer quantity, String unit) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        MaterialUsage material = new MaterialUsage();
        material.setTicket(ticket);
        material.setMaterialName(materialName);
        material.setQuantity(quantity);
        material.setUnit(unit);
        material.setCreatedAt(LocalDateTime.now());
        materialRepository.save(material);

        return ticket;
    }

    public List<TicketProgress> getProgressHistory(String ticketId) {
        return progressRepository.findByTicketIdOrderByCreatedAtDesc(ticketId);
    }

    public List<MaterialUsage> getMaterials(String ticketId) {
        return materialRepository.findByTicketId(ticketId);
    }

    public long countByStatus(Ticket.Status status) {
        return ticketRepository.countByStatus(status);
    }
}
