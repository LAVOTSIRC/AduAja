package com.plr.aduaja.repository;

import com.plr.aduaja.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<Report, String> {

    Optional<Report> findByTicketNumber(String ticketNumber);

    List<Report> findByStatus(Report.ReportStatus status);

    List<Report> findByReporterUserId(String reporterId);

    List<Report> findByStatusOrderBySubmittedAtDesc(Report.ReportStatus status);

    List<Report> findByDescriptionContainingIgnoreCase(String description);

    @Query("SELECT r FROM Report r WHERE r.status = :status AND r.submittedAt BETWEEN :start AND :end")
    List<Report> findByStatusAndDateRange(
            @Param("status") Report.ReportStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    long countByStatus(Report.ReportStatus status);

    long countByReporterUserId(String reporterId);

    List<Report> findByParentReportIsNull();

    List<Report> findByRegionRegionId(String regionId);

    List<Report> findByCategoryCategoryId(String categoryId);
}
