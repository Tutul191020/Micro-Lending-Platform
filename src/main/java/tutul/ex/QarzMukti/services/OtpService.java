package tutul.ex.QarzMukti.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tutul.ex.QarzMukti.common.ValidationException;
import tutul.ex.QarzMukti.entities.Otp;
import tutul.ex.QarzMukti.enums.OtpType;
import tutul.ex.QarzMukti.repositories.OtpRepository;

import java.time.OffsetDateTime;
import java.util.Random;

/**
 * Service for managing OTP operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpRepository otpRepository;

    private static final int OTP_LENGTH = 4;
    private static final int OTP_EXPIRY_MINUTES = 10;
    private static final Random RANDOM = new Random();

    /**
     * Generate a new 4-digit OTP for a user.
     *
     * @param userId  User ID
     * @param otpType Type of OTP (EMAIL or PHONE)
     * @return Generated OTP code
     */
    @Transactional
    public String generateOtp(Long userId, OtpType otpType) {
        log.info("Generating OTP for userId: {}, type: {}", userId, otpType);

        // Generate 4-digit OTP
        String otpCode = String.format("%04d", RANDOM.nextInt(10000));

        // Calculate expiry time
        OffsetDateTime expiresAt = OffsetDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES);

        // Create and save OTP
        Otp otp = Otp.builder()
                .userId(userId)
                .otpCode(otpCode)
                .otpType(otpType)
                .expiresAt(expiresAt)
                .verified(false)
                .build();

        otpRepository.save(otp);

        log.info("OTP generated successfully for userId: {}", userId);
        return otpCode;
    }

    /**
     * Verify OTP code for a user.
     *
     * @param userId  User ID
     * @param otpCode OTP code to verify
     * @return true if OTP is valid and verified
     * @throws ValidationException if OTP is invalid or expired
     */
    @Transactional
    public boolean verifyOtp(Long userId, String otpCode) {
        log.info("Verifying OTP for userId: {}", userId);

        Otp otp = otpRepository.findByUserIdAndOtpCode(userId, otpCode)
                .orElseThrow(() -> new ValidationException("Invalid OTP code"));

        // Check if already verified
        if (otp.getVerified()) {
            throw new ValidationException("OTP has already been used");
        }

        // Check if expired
        if (otp.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new ValidationException("OTP has expired. Please request a new one");
        }

        // Mark as verified
        otp.setVerified(true);
        otpRepository.save(otp);

        log.info("OTP verified successfully for userId: {}", userId);
        return true;
    }

    /**
     * Check if OTP is valid without marking it as verified.
     *
     * @param userId  User ID
     * @param otpCode OTP code to check
     * @return true if OTP is valid and not expired
     */
    @Transactional(readOnly = true)
    public boolean isOtpValid(Long userId, String otpCode) {
        return otpRepository.findByUserIdAndOtpCode(userId, otpCode)
                .map(otp -> !otp.getVerified() && otp.getExpiresAt().isAfter(OffsetDateTime.now()))
                .orElse(false);
    }

    /**
     * Invalidate all OTPs for a user.
     *
     * @param userId User ID
     */
    @Transactional
    public void invalidateUserOtps(Long userId) {
        log.info("Invalidating all OTPs for userId: {}", userId);
        otpRepository.deleteByUserId(userId);
    }

    /**
     * Cleanup expired OTPs (scheduled task - runs every hour).
     */
    @Scheduled(cron = "0 0 * * * *") // Every hour
    @Transactional
    public void cleanupExpiredOtps() {
        log.info("Running scheduled OTP cleanup");
        otpRepository.deleteExpiredOtps(OffsetDateTime.now());

        // Also cleanup verified OTPs older than 24 hours
        OffsetDateTime yesterday = OffsetDateTime.now().minusHours(24);
        otpRepository.deleteVerifiedOtpsBefore(yesterday);

        log.info("OTP cleanup completed");
    }

    /**
     * Get OTP expiry time in seconds.
     *
     * @return Expiry time in seconds
     */
    public int getOtpExpirySeconds() {
        return OTP_EXPIRY_MINUTES * 60;
    }
}
