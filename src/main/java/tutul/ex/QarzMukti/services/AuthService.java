package tutul.ex.QarzMukti.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tutul.ex.QarzMukti.common.UnauthorizedException;
import tutul.ex.QarzMukti.dto.LoginRequest;
import tutul.ex.QarzMukti.dto.LoginResponse;
import tutul.ex.QarzMukti.dto.UserResponse;
import tutul.ex.QarzMukti.entities.User;
import tutul.ex.QarzMukti.repositories.UserRepository;
import tutul.ex.QarzMukti.security.JwtTokenProvider;

/**
 * Service for authentication operations.
 * Handles user login and JWT token generation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;

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
}
