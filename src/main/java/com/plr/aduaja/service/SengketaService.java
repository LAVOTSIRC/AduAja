package com.plr.aduaja.service;

import com.plr.aduaja.model.Sengketa;
import com.plr.aduaja.model.Ticket;
import com.plr.aduaja.model.Report;
import com.plr.aduaja.model.User;
import com.plr.aduaja.repository.SengketaRepository;
import com.plr.aduaja.repository.TicketRepository;
import com.plr.aduaja.repository.ReportRepository;
import com.plr.aduaja.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SengketaService {

    @Autowired
    private SengketaRepository sengketaRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Sengketa> getAllSengketa() {
        return sengketaRepository.findAll();
    }

    public List<Sengketa> getSengketaByStatus(Sengketa.Status status) {
        return sengketaRepository.findByStatusOrderByFiledAtDesc(status);
    }

    public Optional<Sengketa> getSengketaById(String id) {
        return sengketaRepository.findById(id);
    }

    public Optional<Sengketa> getSengketaByNumber(String number) {
        return sengketaRepository.findBySengketaNumber(number);
    }

    public Sengketa createSengketa(String ticketId, String reportId, String filedById, String alasan, Sengketa.Priority prioritas) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        User filedBy = userRepository.findById(filedById)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Sengketa sengketa = new Sengketa();
        sengketa.setSengketaNumber("SGK-" + System.currentTimeMillis());
        sengketa.setTicket(ticket);
        sengketa.setReport(report);
        sengketa.setFiledBy(filedBy);
        sengketa.setAlasan(alasan);
        sengketa.setPrioritas(prioritas);
        sengketa.setStatus(Sengketa.Status.MENUNGGU_TINJAUAN);
        sengketa.setFiledAt(LocalDateTime.now());

        return sengketaRepository.save(sengketa);
    }

    public Sengketa resolveSengketa(String id, String keputusan, String catatan, String resolvedById) {
        Sengketa sengketa = sengketaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sengketa not found"));

        User resolvedBy = userRepository.findById(resolvedById)
                .orElseThrow(() -> new RuntimeException("User not found"));

        sengketa.setKeputusanAdmin(keputusan);
        sengketa.setCatatanAdmin(catatan);
        sengketa.setResolvedBy(resolvedBy);
        sengketa.setResolvedAt(LocalDateTime.now());
        sengketa.setStatus(Sengketa.Status.DIPROSES);

        return sengketaRepository.save(sengketa);
    }

    public long countByStatus(Sengketa.Status status) {
        return sengketaRepository.countByStatus(status);
    }
}
