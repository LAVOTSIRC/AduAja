package com.plr.aduaja.repository;

import com.plr.aduaja.model.SlaRecord;
import com.plr.aduaja.model.SlaRecord.SlaStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SlaRecordRepository extends JpaRepository<SlaRecord, String> {

    Optional<SlaRecord> findByReportReportId(String reportId);

    List<SlaRecord> findByCurrentStatus(SlaStatus status);

    List<SlaRecord> findBySlaDeadlineAtBeforeAndCurrentStatusNot(LocalDateTime now, SlaStatus completedStatus);

    long countByCurrentStatus(SlaStatus status);
}
