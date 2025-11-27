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
}
