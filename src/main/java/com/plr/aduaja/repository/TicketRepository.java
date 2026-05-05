package com.plr.aduaja.repository;

import com.plr.aduaja.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, String> {

    Optional<Ticket> findByTicketNumber(String ticketNumber);

    List<Ticket> findByAssignedPetugasId(String petugasId);

    List<Ticket> findByAssignedPetugasIdAndStatus(String petugasId, Ticket.Status status);

    List<Ticket> findByStatus(Ticket.Status status);

    List<Ticket> findByStatusOrderBySlaDeadlineAsc(Ticket.Status status);

    List<Ticket> findByAssignedByDinasId(String dinasId);

    @Query("SELECT t FROM Ticket t WHERE t.assignedPetugas.id = :petugasId ORDER BY t.createdAt DESC")
    List<Ticket> findByPetugasOrderByCreatedAtDesc(@Param("petugasId") String petugasId);

    long countByStatus(Ticket.Status status);

    long countByAssignedPetugasIdAndStatus(String petugasId, Ticket.Status status);

    List<Ticket> findByParentTicketId(String parentTicketId);

    List<Ticket> findByParentTicketIsNull();
}
