package com.plr.aduaja.repository;

import com.plr.aduaja.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, String> {

    List<Report> findByStatus(Report.Status status);

    List<Report> findByReporterId(String reporterId);

    List<Report> findByCategory(String category);

    List<Report> findByStatusOrderByCreatedAtDesc(Report.Status status);

    List<Report> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String title, String description);

    @Query("SELECT r FROM Report r WHERE r.status = :status AND r.createdAt BETWEEN :start AND :end")
    List<Report> findByStatusAndDateRange(
            @Param("status") Report.Status status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    long countByStatus(Report.Status status);

    long countByReporterId(String reporterId);

    List<Report> findByDisposisiIsNullAndStatusIn(List<Report.Status> statuses);

    List<Report> findBySimilarityScoreGreaterThan(int score);

    @Query("SELECT r FROM Report r JOIN r.disposisi d WHERE d.targetDinas.id = :dinasId")
    List<Report> findByDisposisiDinasId(@Param("dinasId") String dinasId);
}
