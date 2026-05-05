package com.plr.aduaja.repository;

import com.plr.aduaja.model.MaterialUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaterialUsageRepository extends JpaRepository<MaterialUsage, String> {

    List<MaterialUsage> findByTicketId(String ticketId);
}
