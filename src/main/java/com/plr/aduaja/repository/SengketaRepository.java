package com.plr.aduaja.repository;

import com.plr.aduaja.model.Sengketa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SengketaRepository extends JpaRepository<Sengketa, String> {

    Optional<Sengketa> findBySengketaNumber(String sengketaNumber);

    List<Sengketa> findByStatus(Sengketa.Status status);

    List<Sengketa> findByFiledById(String filedById);

    List<Sengketa> findByTicketId(String ticketId);

    List<Sengketa> findByReportId(String reportId);

    List<Sengketa> findByStatusOrderByFiledAtDesc(Sengketa.Status status);

    long countByStatus(Sengketa.Status status);
}
