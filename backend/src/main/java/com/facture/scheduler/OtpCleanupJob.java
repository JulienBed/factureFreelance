package com.facture.scheduler;

import com.facture.entity.User;
import io.quarkus.logging.Log;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled job to clean up expired OTP tokens
 * Runs every day at 3 AM
 */
@ApplicationScoped
public class OtpCleanupJob {

    /**
     * Clean up expired OTP codes every day at 3 AM
     * Removes OTP secret and expiry from users with expired OTPs
     */
    @Scheduled(cron = "0 0 3 * * ?") // Every day at 3 AM
    @Transactional
    public void cleanupExpiredOtps() {
        Log.info("🧹 [Job] Starting OTP cleanup job...");

        LocalDateTime now = LocalDateTime.now();

        // Find all users with expired OTPs
        List<User> usersWithExpiredOtp = User.list(
            "otpSecret is not null and otpExpiry < ?1",
            now
        );

        if (usersWithExpiredOtp.isEmpty()) {
            Log.info("✅ No expired OTPs to clean up");
            return;
        }

        int count = 0;
        for (User user : usersWithExpiredOtp) {
            user.otpSecret = null;
            user.otpExpiry = null;
            count++;
            Log.infof("🧹 Cleaned up expired OTP for user: %s (expired: %s)",
                user.email, user.otpExpiry);
        }

        Log.infof("🧹 [Job] OTP cleanup complete. Cleaned %d expired OTPs", count);
    }
}
