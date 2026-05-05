package com.plr.aduaja.repository;

import com.plr.aduaja.model.ConfirmationRequest;
import com.plr.aduaja.model.ConfirmationRequest.ResponseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConfirmationRequestRepository extends JpaRepository<ConfirmationRequest, String> {

    Optional<ConfirmationRequest> findByReportReportId(String reportId);

    Optional<ConfirmationRequest> findByWargaUserId(String wargaId);

    List<ConfirmationRequest> findByDeadlineAtBeforeAndResponseIsNull(LocalDateTime now);

    List<ConfirmationRequest> findByIsLockedFalse();
}
