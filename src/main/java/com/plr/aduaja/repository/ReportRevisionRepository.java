package com.plr.aduaja.repository;

import com.plr.aduaja.model.Report;
import com.plr.aduaja.model.ReportRevision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRevisionRepository extends JpaRepository<ReportRevision, String> {

    List<ReportRevision> findByReportReportId(String reportId);

    List<ReportRevision> findByReportOrderByChangedAtDesc(Report report);

    List<ReportRevision> findByChangedBy(String changedBy);

    long countByReportReportId(String reportId);

    long countByReport(Report report);
}
