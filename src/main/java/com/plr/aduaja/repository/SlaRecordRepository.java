package com.plr.aduaja.repository;

import com.plr.aduaja.model.SlaRecord;
import com.plr.aduaja.model.SlaRecord.SlaStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SlaRecordRepository extends JpaRepository<SlaRecord, String> {

    Optional<SlaRecord> findByReportReportId(String reportId);

    List<SlaRecord> findByCurrentStatus(SlaStatus status);

    List<SlaRecord> findBySlaDeadlineAtBeforeAndCurrentStatusNot(LocalDateTime now, SlaStatus completedStatus);

    @Query("SELECT s FROM SlaRecord s WHERE s.currentStatus = 'BERJALAN' AND s.slaDeadlineAt < :now")
    List<SlaRecord> findOverdue(@Param("now") LocalDateTime now);

    @Query("SELECT s FROM SlaRecord s WHERE s.createdAt BETWEEN :start AND :end")
    List<SlaRecord> findByCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    long countByCurrentStatus(SlaStatus status);
}

