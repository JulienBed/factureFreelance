package com.facture.util;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@ApplicationScoped
public class OtpUtil {

    @ConfigProperty(name = "otp.expiration.minutes", defaultValue = "5")
    int otpExpirationMinutes;

    @ConfigProperty(name = "otp.length", defaultValue = "6")
    int otpLength;

    private final SecureRandom random = new SecureRandom();

    public String generateOtp() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < otpLength; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }

    public LocalDateTime getOtpExpiry() {
        return LocalDateTime.now().plusMinutes(otpExpirationMinutes);
    }

    public boolean isOtpExpired(LocalDateTime otpExpiry) {
        return otpExpiry == null || LocalDateTime.now().isAfter(otpExpiry);
    }
}
