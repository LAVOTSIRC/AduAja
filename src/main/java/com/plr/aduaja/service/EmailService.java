package com.plr.aduaja.service;

import com.plr.aduaja.model.OtpVerification;

public interface EmailService {

    void sendOtpEmail(String to, String otpCode, OtpVerification.OtpType type);

    void sendEmail(String to, String subject, String htmlBody);
}
