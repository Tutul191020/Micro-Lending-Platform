package tutul.ex.QarzMukti.shared.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tutul.ex.QarzMukti.shared.entities.Otp;
import tutul.ex.QarzMukti.shared.enums.OtpType;

import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * Repository for OTP entity operations.
 */
@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {

    /**
     * Find the most recent active (unverified and not expired) OTP for a user and
     * type.
     */
    @Query("SELECT o FROM Otp o WHERE o.userId = :userId AND o.otpType = :otpType " +
            "AND o.verified = false AND o.expiresAt > :now ORDER BY o.createdAt DESC")
    Optional<Otp> findActiveOtpByUserIdAndType(
            @Param("userId") Long userId,
            @Param("otpType") OtpType otpType,
            @Param("now") OffsetDateTime now);

    /**
     * Find OTP by user ID and OTP code.
     */
    Optional<Otp> findByUserIdAndOtpCode(Long userId, String otpCode);

    /**
     * Delete all expired OTPs.
     */
    @Modifying
    @Query("DELETE FROM Otp o WHERE o.expiresAt < :now")
    void deleteExpiredOtps(@Param("now") OffsetDateTime now);

    /**
     * Delete all OTPs for a specific user.
     */
    @Modifying
    @Query("DELETE FROM Otp o WHERE o.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    /**
     * Delete all verified OTPs older than specified time.
     */
    @Modifying
    @Query("DELETE FROM Otp o WHERE o.verified = true AND o.createdAt < :before")
    void deleteVerifiedOtpsBefore(@Param("before") OffsetDateTime before);
}
