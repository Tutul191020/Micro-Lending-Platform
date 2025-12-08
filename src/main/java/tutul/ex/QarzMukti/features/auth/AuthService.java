package tutul.ex.QarzMukti.features.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tutul.ex.QarzMukti.shared.common.ResourceNotFoundException;
import tutul.ex.QarzMukti.shared.common.UnauthorizedException;
import tutul.ex.QarzMukti.shared.common.ValidationException;
import tutul.ex.QarzMukti.shared.dto.*;
import tutul.ex.QarzMukti.shared.entities.User;
import tutul.ex.QarzMukti.shared.enums.OtpType;
import tutul.ex.QarzMukti.shared.repositories.UserRepository;
import tutul.ex.QarzMukti.shared.security.JwtTokenProvider;
import tutul.ex.QarzMukti.shared.NotificationService;

import java.time.OffsetDateTime;

/**
 * Service for authentication operations.
 * Handles user login, JWT token generation, and password reset.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final OtpService otpService;
    private final NotificationService notificationService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Authenticate user and generate JWT token.
     *
     * @param request Login credentials
     * @return LoginResponse with JWT token and user details
     * @throws UnauthorizedException if credentials are invalid
     */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String loginIdentifier = request.getUsername();
        if (loginIdentifier == null || loginIdentifier.isEmpty()) {
            loginIdentifier = request.getEmail();
        }

        if (loginIdentifier == null || loginIdentifier.isEmpty()) {
            throw new UnauthorizedException("Username or Email is required");
        }

        log.info("Login attempt for: {}", loginIdentifier);

        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginIdentifier,
                            request.getPassword()));

            // Generate JWT token
            String token = tokenProvider.generateToken(authentication);

            log.info("User {} logged in successfully", loginIdentifier);

            return LoginResponse.of(
                    token,
                    tokenProvider.getExpirationInSeconds());

        } catch (AuthenticationException e) {
            log.error("Authentication failed for: {}", loginIdentifier);
            throw new UnauthorizedException("Invalid username/email or password");
        }
    }

    /**
     * Convert User entity to UserResponse DTO.
     */
    private UserResponse convertToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .dateOfBirth(user.getDateOfBirth())
                .enabled(user.getEnabled())
                .verificationStatus(user.getVerificationStatus())
                .verifiedBy(user.getVerifiedBy())
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * Initiate forgot password flow by sending OTP.
     *
     * @param request Forgot password request with email or phone
     * @return OTP response with masked identifier
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional
    public OtpResponse initiateForgotPassword(ForgotPasswordRequest request) {
        String identifier = request.getIdentifier().trim();
        log.info("Forgot password initiated for identifier: {}", maskIdentifier(identifier));

        // Find user by email or phone
        User user = findUserByIdentifier(identifier);

        // Determine OTP type based on identifier
        OtpType otpType = identifier.contains("@") ? OtpType.EMAIL : OtpType.PHONE;

        // Generate OTP
        String otpCode = otpService.generateOtp(user.getId(), otpType);

        // Send OTP via appropriate channel
        if (otpType == OtpType.EMAIL) {
            notificationService.sendEmailOtp(user.getEmail(), otpCode);
        } else {
            notificationService.sendSmsOtp(user.getPhone(), otpCode);
        }

        log.info("OTP sent successfully for user: {}", user.getId());

        return OtpResponse.builder()
                .message("OTP has been sent to your " + (otpType == OtpType.EMAIL ? "email" : "phone"))
                .expiresIn(otpService.getOtpExpirySeconds())
                .otpSentTo(maskIdentifier(otpType == OtpType.EMAIL ? user.getEmail() : user.getPhone()))
                .build();
    }

    /**
     * Verify OTP code for password reset.
     *
     * @param request Verify OTP request
     * @throws ResourceNotFoundException if user not found
     * @throws ValidationException       if OTP is invalid
     */
    @Transactional(readOnly = true)
    public void verifyOtpForReset(VerifyOtpRequest request) {
        String identifier = request.getIdentifier().trim();
        log.info("Verifying OTP for identifier: {}", maskIdentifier(identifier));

        User user = findUserByIdentifier(identifier);

        // Verify OTP (will throw exception if invalid)
        boolean isValid = otpService.isOtpValid(user.getId(), request.getOtpCode());

        if (!isValid) {
            throw new ValidationException("Invalid or expired OTP");
        }

        log.info("OTP verified successfully for user: {}", user.getId());
    }

    /**
     * Reset password using OTP verification.
     *
     * @param request Reset password request with OTP
     * @throws ResourceNotFoundException if user not found
     * @throws ValidationException       if OTP is invalid or passwords don't match
     */
    @Transactional
    public void resetPasswordWithOtp(ResetPasswordRequest request) {
        String identifier = request.getIdentifier().trim();
        log.info("Password reset attempt for identifier: {}", maskIdentifier(identifier));

        // Validate password confirmation
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ValidationException("New password and confirmation do not match");
        }

        // Find user
        User user = findUserByIdentifier(identifier);

        // Verify and mark OTP as used
        otpService.verifyOtp(user.getId(), request.getOtpCode());

        // Update password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(OffsetDateTime.now());
        userRepository.save(user);

        // Invalidate all remaining OTPs for this user
        otpService.invalidateUserOtps(user.getId());

        log.info("Password reset successfully for user: {}", user.getId());
    }

    /**
     * Find user by email or phone number.
     *
     * @param identifier Email or phone number
     * @return User entity
     * @throws ResourceNotFoundException if user not found
     */
    private User findUserByIdentifier(String identifier) {
        if (identifier.contains("@")) {
            return userRepository.findByEmail(identifier)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + identifier));
        } else {
            return userRepository.findByPhone(identifier)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with phone: " + identifier));
        }
    }

    /**
     * Mask email or phone for privacy.
     *
     * @param identifier Email or phone
     * @return Masked identifier
     */
    private String maskIdentifier(String identifier) {
        if (identifier == null || identifier.length() <= 4) {
            return "***";
        }

        if (identifier.contains("@")) {
            int atIndex = identifier.indexOf("@");
            String localPart = identifier.substring(0, atIndex);
            String domain = identifier.substring(atIndex);

            if (localPart.length() <= 2) {
                return "***" + domain;
            }
            return localPart.charAt(0) + "***" + localPart.charAt(localPart.length() - 1) + domain;
        } else {
            return "***" + identifier.substring(identifier.length() - 4);
        }
    }
}
