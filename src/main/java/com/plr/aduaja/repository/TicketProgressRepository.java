package com.plr.aduaja.repository;

import com.plr.aduaja.model.TicketProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketProgressRepository extends JpaRepository<TicketProgress, String> {

    List<TicketProgress> findByTicketId(String ticketId);

    List<TicketProgress> findByUpdatedById(String updatedById);

    List<TicketProgress> findByTicketIdOrderByCreatedAtDesc(String ticketId);
}
