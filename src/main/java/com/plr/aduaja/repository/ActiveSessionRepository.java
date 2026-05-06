package com.plr.aduaja.repository;

import com.plr.aduaja.model.ActiveSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ActiveSessionRepository extends JpaRepository<ActiveSession, String> {

    List<ActiveSession> findByUserUserIdAndIsActiveTrue(String userId);

    Optional<ActiveSession> findBySessionIdAndIsActiveTrue(String sessionId);

    List<ActiveSession> findByExpiresAtBeforeAndIsActiveTrue(LocalDateTime now);

    void deleteByUserUserId(String userId);
}
