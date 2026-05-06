package com.plr.aduaja.service;

import com.plr.aduaja.model.*;
import com.plr.aduaja.model.OtpVerification.OtpType;
import com.plr.aduaja.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OtpService {

    @Autowired
    private OtpVerificationRepository otpRepository;

    @Autowired
    private UserRepository userRepository;

    public OtpVerification generateOtp(String userId, OtpType type, int expiryMinutes) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        OtpVerification otp = new OtpVerification();
        otp.setUser(user);
        otp.setOtpCode(generateCode());
        otp.setOtpType(type);
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(expiryMinutes));
        otp.setIsUsed(false);
        otp.setCreatedAt(LocalDateTime.now());

        return otpRepository.save(otp);
    }

    public boolean verifyOtp(String userId, String otpCode, OtpType type) {
        Optional<OtpVerification> otpOpt = otpRepository.findTopByUserUserIdAndOtpTypeOrderByCreatedAtDesc(userId, type);
        if (otpOpt.isEmpty()) {
            return false;
        }

        OtpVerification otp = otpOpt.get();
        if (otp.getIsUsed() || otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            return false;
        }

        if (!otp.getOtpCode().equals(otpCode)) {
            return false;
        }

        otp.setIsUsed(true);
        otpRepository.save(otp);
        return true;
    }

    public void expireOldOtps() {
        List<OtpVerification> expired = otpRepository.findByExpiresAtBeforeAndIsUsedFalse(LocalDateTime.now());
        for (OtpVerification otp : expired) {
            otp.setIsUsed(true);
        }
        otpRepository.saveAll(expired);
    }

    private String generateCode() {
        return String.format("%06d", (int) (Math.random() * 1000000));
    }
}
