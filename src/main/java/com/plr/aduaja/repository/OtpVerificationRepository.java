package com.plr.aduaja.repository;

import com.plr.aduaja.model.OtpVerification;
import com.plr.aduaja.model.OtpVerification.OtpType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// ============================================================
// Repository untuk OTP Verification
// Digunakan oleh OtpServiceImpl untuk verifikasi OTP
// ============================================================
@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, String> {

    // Cari OTP berdasarkan userId + otpCode + belum dipakai
    // Method ini dipanggil oleh OtpServiceImpl.verifyOtp()
    Optional<OtpVerification> findByUserUserIdAndOtpCodeAndIsUsedFalse(String userId, String otpCode);

    // Cari OTP aktif berdasarkan userId (belum dipakai)
    Optional<OtpVerification> findByUserUserIdAndIsUsedFalse(String userId);

    // Semua OTP berdasarkan userId
    List<OtpVerification> findByUserUserId(String userId);

    // OTP aktif berdasarkan userId + type
    List<OtpVerification> findByUserUserIdAndOtpTypeAndIsUsedFalse(String userId, OtpType otpType);

    // OTP yang sudah expired tapi belum dipakai
    List<OtpVerification> findByIsUsedFalseAndIsVerifiedFalse();
}
