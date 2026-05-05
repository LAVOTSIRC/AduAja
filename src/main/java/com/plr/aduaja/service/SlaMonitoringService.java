package com.plr.aduaja.service;

import com.plr.aduaja.model.Report;
import com.plr.aduaja.model.Ticket;
import com.plr.aduaja.repository.ReportRepository;
import com.plr.aduaja.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SlaMonitoringService {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Scheduled(fixedRate = 3600000)
    public void checkSlaViolations() {
        LocalDateTime now = LocalDateTime.now();
        List<Report> reports = reportRepository.findAll();
        List<Ticket> tickets = ticketRepository.findAll();

        for (Report report : reports) {
            if (report.getSlaDeadline() != null &&
                    report.getSlaDeadline().isBefore(now) &&
                    (report.getStatus() == Report.Status.MENUNGGU ||
                            report.getStatus() == Report.Status.DIPROSES)) {
                System.out.println("SLA Violation - Report: " + report.getId() +
                        " - Deadline: " + report.getSlaDeadline());
            }
        }

        for (Ticket ticket : tickets) {
            if (ticket.getSlaDeadline() != null &&
                    ticket.getSlaDeadline().isBefore(now) &&
                    (ticket.getStatus() == Ticket.Status.BARU ||
                            ticket.getStatus() == Ticket.Status.IN_PROGRESS)) {
                System.out.println("SLA Violation - Ticket: " + ticket.getTicketNumber() +
                        " - Deadline: " + ticket.getSlaDeadline());
            }
        }
    }

    public Map<String, Object> getSlaStatistics() {
        LocalDateTime now = LocalDateTime.now();
        Map<String, Object> stats = new HashMap<>();

        long totalReports = reportRepository.count();
        long totalTickets = ticketRepository.count();

        long onTimeReports = 0;
        long lateReports = 0;
        for (Report report : reportRepository.findAll()) {
            if (report.getSlaDeadline() != null) {
                if (report.getSlaDeadline().isBefore(now) &&
                        (report.getStatus() == Report.Status.MENUNGGU ||
                                report.getStatus() == Report.Status.DIPROSES)) {
                    lateReports++;
                } else if (report.getStatus() == Report.Status.SELESAI) {
                    onTimeReports++;
                }
            }
        }

        long onTimeTickets = 0;
        long lateTickets = 0;
        for (Ticket ticket : ticketRepository.findAll()) {
            if (ticket.getSlaDeadline() != null) {
                if (ticket.getSlaDeadline().isBefore(now) &&
                        (ticket.getStatus() == Ticket.Status.BARU ||
                                ticket.getStatus() == Ticket.Status.IN_PROGRESS)) {
                    lateTickets++;
                } else if (ticket.getStatus() == Ticket.Status.SELESAI) {
                    onTimeTickets++;
                }
            }
        }

        stats.put("totalReports", totalReports);
        stats.put("totalTickets", totalTickets);
        stats.put("onTimeReports", onTimeReports);
        stats.put("lateReports", lateReports);
        stats.put("onTimeTickets", onTimeTickets);
        stats.put("lateTickets", lateTickets);

        return stats;
    }

    public List<Map<String, Object>> getLateItems() {
        LocalDateTime now = LocalDateTime.now();
        List<Map<String, Object>> lateItems = new ArrayList<>();

        for (Report report : reportRepository.findAll()) {
            if (report.getSlaDeadline() != null &&
                    report.getSlaDeadline().isBefore(now) &&
                    (report.getStatus() == Report.Status.MENUNGGU ||
                            report.getStatus() == Report.Status.DIPROSES)) {

                Map<String, Object> item = new HashMap<>();
                item.put("type", "Report");
                item.put("id", report.getId());
                item.put("title", report.getTitle());
                item.put("status", report.getStatus().toString());
                item.put("slaDeadline", report.getSlaDeadline());
                long hoursLate = java.time.Duration.between(report.getSlaDeadline(), now).toHours();
                item.put("hoursLate", hoursLate);
                lateItems.add(item);
            }
        }

        for (Ticket ticket : ticketRepository.findAll()) {
            if (ticket.getSlaDeadline() != null &&
                    ticket.getSlaDeadline().isBefore(now) &&
                    (ticket.getStatus() == Ticket.Status.BARU ||
                            ticket.getStatus() == Ticket.Status.IN_PROGRESS)) {

                Map<String, Object> item = new HashMap<>();
                item.put("type", "Ticket");
                item.put("id", ticket.getId());
                item.put("ticketNumber", ticket.getTicketNumber());
                item.put("status", ticket.getStatus().toString());
                item.put("slaDeadline", ticket.getSlaDeadline());
                long hoursLate = java.time.Duration.between(ticket.getSlaDeadline(), now).toHours();
                item.put("hoursLate", hoursLate);
                lateItems.add(item);
            }
        }

        return lateItems;
    }
}
