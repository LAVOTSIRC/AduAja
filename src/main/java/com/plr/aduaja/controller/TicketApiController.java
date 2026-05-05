package com.plr.aduaja.controller;

import com.plr.aduaja.model.Ticket;
import com.plr.aduaja.model.TicketProgress;
import com.plr.aduaja.model.MaterialUsage;
import com.plr.aduaja.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/tickets")
public class TicketApiController {

    @Autowired
    private TicketService ticketService;

    @GetMapping
    public ResponseEntity<List<Ticket>> getAllTickets() {
        return ResponseEntity.ok(ticketService.getAllTickets());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ticket> getTicketById(@PathVariable String id) {
        Optional<Ticket> ticket = ticketService.getTicketById(id);
        return ticket.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/number/{ticketNumber}")
    public ResponseEntity<Ticket> getTicketByNumber(@PathVariable String ticketNumber) {
        Optional<Ticket> ticket = ticketService.getTicketByNumber(ticketNumber);
        return ticket.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Ticket>> getTicketsByStatus(@PathVariable Ticket.Status status) {
        return ResponseEntity.ok(ticketService.getTicketsByStatus(status));
    }

    @GetMapping("/petugas/{petugasId}")
    public ResponseEntity<List<Ticket>> getTicketsByPetugas(@PathVariable String petugasId) {
        return ResponseEntity.ok(ticketService.getTicketsByPetugas(petugasId));
    }

    @GetMapping("/petugas/{petugasId}/active")
    public ResponseEntity<List<Ticket>> getActiveTicketsByPetugas(@PathVariable String petugasId) {
        return ResponseEntity.ok(ticketService.getActiveTicketsByPetugas(petugasId));
    }

    @GetMapping("/{ticketId}/progress")
    public ResponseEntity<List<TicketProgress>> getProgressHistory(@PathVariable String ticketId) {
        return ResponseEntity.ok(ticketService.getProgressHistory(ticketId));
    }

    @GetMapping("/{ticketId}/materials")
    public ResponseEntity<List<MaterialUsage>> getMaterials(@PathVariable String ticketId) {
        return ResponseEntity.ok(ticketService.getMaterials(ticketId));
    }

    @PostMapping
    public ResponseEntity<Ticket> createTicket(@RequestParam String reportId,
                                                @RequestParam String petugasId,
                                                @RequestParam String assignedById) {
        try {
            Ticket ticket = ticketService.createTicket(
                    ticketService.getTicketById(reportId).orElseThrow().getReport(),
                    null, null);
            return ResponseEntity.status(HttpStatus.CREATED).body(ticket);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<Ticket> startTicket(@PathVariable String id,
                                               @RequestParam String petugasId) {
        try {
            return ResponseEntity.ok(ticketService.startTicket(id, petugasId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<Ticket> completeTicket(@PathVariable String id,
                                                  @RequestParam String notes) {
        try {
            return ResponseEntity.ok(ticketService.completeTicket(id, notes));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/{id}/pending")
    public ResponseEntity<Ticket> pendingTicket(@PathVariable String id,
                                                 @RequestParam String reason) {
        try {
            return ResponseEntity.ok(ticketService.pendingTicket(id, reason));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/{id}/pause")
    public ResponseEntity<Ticket> pauseTicket(@PathVariable String id,
                                               @RequestParam String reason) {
        try {
            return ResponseEntity.ok(ticketService.pauseTicket(id, reason));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/{id}/resume")
    public ResponseEntity<Ticket> resumeTicket(@PathVariable String id) {
        try {
            return ResponseEntity.ok(ticketService.resumeTicket(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/{id}/escalate")
    public ResponseEntity<Ticket> escalateTicket(@PathVariable String id) {
        try {
            return ResponseEntity.ok(ticketService.updateStatus(id, Ticket.Status.ESKALASI));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/{id}/progress")
    public ResponseEntity<Ticket> addProgress(@PathVariable String id,
                                               @RequestParam String petugasId,
                                               @RequestParam String keterangan,
                                               @RequestParam(required = false) String estimasi) {
        try {
            return ResponseEntity.ok(ticketService.addProgress(id, petugasId, keterangan, estimasi));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/{id}/materials")
    public ResponseEntity<Ticket> addMaterial(@PathVariable String id,
                                               @RequestParam String materialName,
                                               @RequestParam Integer quantity,
                                               @RequestParam String unit) {
        try {
            return ResponseEntity.ok(ticketService.addMaterial(id, materialName, quantity, unit));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Ticket> updateStatus(@PathVariable String id,
                                                @RequestParam Ticket.Status status) {
        try {
            return ResponseEntity.ok(ticketService.updateStatus(id, status));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getTicketCounts() {
        Map<String, Long> counts = Map.of(
                "baru", ticketService.countByStatus(Ticket.Status.BARU),
                "in_progress", ticketService.countByStatus(Ticket.Status.IN_PROGRESS),
                "pending", ticketService.countByStatus(Ticket.Status.PENDING),
                "selesai", ticketService.countByStatus(Ticket.Status.SELESAI)
        );
        return ResponseEntity.ok(counts);
    }
}
