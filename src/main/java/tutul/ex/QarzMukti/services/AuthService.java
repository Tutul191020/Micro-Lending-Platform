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
        log.info("Login attempt for username: {}", request.getUsername());

        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()));

            // Generate JWT token
            String token = tokenProvider.generateToken(authentication);

            // Get user details
            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new UnauthorizedException("User not found"));

            // Convert to UserResponse
            UserResponse userResponse = convertToUserResponse(user);

            log.info("User {} logged in successfully", request.getUsername());

            return LoginResponse.of(
                    token,
                    tokenProvider.getExpirationInSeconds(),
                    userResponse);

        } catch (AuthenticationException e) {
            log.error("Authentication failed for username: {}", request.getUsername());
            throw new UnauthorizedException("Invalid username or password");
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
                .phone(user.getPhone())
                .nationalId(user.getNationalId())
                .dateOfBirth(user.getDateOfBirth())
                .address(user.getAddress())
                .city(user.getCity())
                .postalCode(user.getPostalCode())
                .enabled(user.getEnabled())
                .verified(user.getVerified())
                .verificationStatus(user.getVerificationStatus())
                .verificationComments(user.getVerificationComments())
                .verifiedBy(user.getVerifiedBy())
                .verifiedAt(user.getVerifiedAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
