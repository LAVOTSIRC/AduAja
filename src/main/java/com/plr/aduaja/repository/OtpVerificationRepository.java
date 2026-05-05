package com.plr.aduaja.repository;

import com.plr.aduaja.model.OtpVerification;
import com.plr.aduaja.model.OtpVerification.OtpType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, String> {

    Optional<OtpVerification> findByUserUserIdAndOtpCodeAndIsUsedFalse(String userId, String otpCode);

    List<OtpVerification> findByUserUserIdAndOtpTypeAndIsUsedFalse(String userId, OtpType otpType);

    List<OtpVerification> findByExpiresAtBeforeAndIsUsedFalse(LocalDateTime now);

    Optional<OtpVerification> findTopByUserUserIdAndOtpTypeOrderByCreatedAtDesc(String userId, OtpType otpType);
}
