package com.plr.aduaja.repository;

import com.plr.aduaja.model.Disposition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DispositionRepository extends JpaRepository<Disposition, String> {

    Optional<Disposition> findByReportReportId(String reportId);

    List<Disposition> findByTargetAgencyAgencyId(String agencyId);

    List<Disposition> findByDispatchedByUserId(String dispatchedByUserId);

    List<Disposition> findByTargetAgencyAgencyIdOrderByDispatchedAtDesc(String agencyId);
}
