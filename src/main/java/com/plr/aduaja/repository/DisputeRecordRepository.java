package com.plr.aduaja.repository;

import com.plr.aduaja.model.DisputeRecord;
import com.plr.aduaja.model.DisputeRecord.ResolutionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DisputeRecordRepository extends JpaRepository<DisputeRecord, String> {

    Optional<DisputeRecord> findByReportReportId(String reportId);

    List<DisputeRecord> findByFiledByUserId(String filedByUserId);

    List<DisputeRecord> findByResolvedByUserId(String resolvedByUserId);

    List<DisputeRecord> findByResolutionIsNull();

    long countByResolutionIsNull();
}
