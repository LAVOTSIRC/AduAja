package com.plr.aduaja.repository;

import com.plr.aduaja.model.SlaPauseLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SlaPauseLogRepository extends JpaRepository<SlaPauseLog, String> {

    List<SlaPauseLog> findBySlaRecordSlaId(String slaId);

    List<SlaPauseLog> findBySlaRecordSlaIdOrderByPausedAtDesc(String slaId);

    List<SlaPauseLog> findByPausedByUserId(String pausedByUserId);
}
