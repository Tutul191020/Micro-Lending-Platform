package tutul.ex.QarzMukti.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tutul.ex.QarzMukti.constants.ApiEndpoints;
import tutul.ex.QarzMukti.dto.*;
import tutul.ex.QarzMukti.services.AuthService;
import tutul.ex.QarzMukti.services.UserService;

@Slf4j
@RestController
@RequestMapping(ApiEndpoints.Auth.BASE)
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    @PostMapping(ApiEndpoints.Auth.LOGIN)
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for username: {}", request.getUsername());
        LoginResponse response = authService.login(request);
        log.info("User {} logged in successfully", request.getUsername());
        return new ResponseEntity<>(ApiResponse.success(response, "Login successful"), HttpStatus.OK);
    }

    @PostMapping(ApiEndpoints.Auth.REGISTER)
    public ResponseEntity<ApiResponse<UserResponse>> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        log.info("New user registration attempt for username: {}", request.getUsername());
        UserResponse response = userService.registerUser(request);
        log.info("User {} registered successfully with status: {}", request.getUsername(),
                response.getVerificationStatus());
        return new ResponseEntity<>(
                ApiResponse.success(response,
                        "User registered successfully. Your account is pending verification by admin."),
                HttpStatus.CREATED);
    }

    @PutMapping(ApiEndpoints.Auth.CHANGE_PASSWORD)
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody UserPasswordChangeRequest request) {
        String userId = String.valueOf(userService.getCurrentAuthenticatedUserId());
        log.info("Password change request for authenticated user: {}", userId);
        userService.changePassword(userId, request);
        log.info("Password changed successfully for user: {}", userId);
        return new ResponseEntity<>(ApiResponse.success(null, "Password changed successfully"), HttpStatus.OK);
    }

    @PostMapping(ApiEndpoints.Auth.FORGOT_PASSWORD)
    public ResponseEntity<ApiResponse<OtpResponse>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        log.info("Forgot password request for identifier: {}", request.getIdentifier());
        OtpResponse response = authService.initiateForgotPassword(request);
        log.info("OTP sent successfully for forgot password");
        return new ResponseEntity<>(ApiResponse.success(response, "OTP sent successfully"), HttpStatus.OK);
    }

    @PostMapping(ApiEndpoints.Auth.VERIFY_OTP)
    public ResponseEntity<ApiResponse<Void>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        log.info("OTP verification request for identifier: {}", request.getIdentifier());
        authService.verifyOtpForReset(request);
        log.info("OTP verified successfully");
        return new ResponseEntity<>(ApiResponse.success(null, "OTP verified successfully"), HttpStatus.OK);
    }

    @PostMapping(ApiEndpoints.Auth.RESET_PASSWORD)
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("Password reset request for identifier: {}", request.getIdentifier());
        authService.resetPasswordWithOtp(request);
        log.info("Password reset successfully");
        return new ResponseEntity<>(ApiResponse.success(null, "Password reset successfully"), HttpStatus.OK);
    }
}
