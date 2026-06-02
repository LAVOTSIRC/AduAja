package com.plr.aduaja.repository;

import com.plr.aduaja.model.ValidationDecision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ValidationDecisionRepository extends JpaRepository<ValidationDecision, String> {

    List<ValidationDecision> findByReportReportId(String reportId);

    List<ValidationDecision> findByAdminUserId(String adminId);

    List<ValidationDecision> findByReportReportIdOrderByDecidedAtDesc(String reportId);
}
