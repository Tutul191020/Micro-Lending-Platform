package tutul.ex.QarzMukti.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tutul.ex.QarzMukti.dto.*;
import tutul.ex.QarzMukti.services.UserService;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    /**
     * Self-registration endpoint
     * User can register but cannot select roles
     * Status will be PENDING until verified by SUPER_USER
     * POST /api/users/register
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> registerUser(
            @Valid @RequestBody UserRegistrationRequest request) {
        log.info("REST: Register user request received for username: {}", request.getUsername());
        
        UserResponse response = userService.registerUser(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, 
                        "User registered successfully. Your account is pending verification by admin."));
    }

    /**
     * Upload document for user
     * POST /api/users/{userId}/documents/upload
     * @param documentType: NATIONAL_ID, ELECTRICITY_BILL, PROFILE_PHOTO
     */
    @PostMapping("/{userId}/documents/upload")
    public ResponseEntity<ApiResponse<DocumentUploadResponse>> uploadDocument(
            @PathVariable @NotNull String userId,
            @RequestParam("documentType") String documentType,
            @RequestParam("file") MultipartFile file) {
        log.info("REST: Upload document request for user: {}, type: {}", userId, documentType);
        
        DocumentUploadResponse response = userService.uploadDocument(userId, documentType, file);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Document uploaded successfully"));
    }

    /**
     * Verify/Approve user by SUPER_USER
     * Can assign roles during verification
     * PUT /api/users/verify
     * Requires: SUPER_USER role
     */
    @PutMapping("/verify")
    public ResponseEntity<ApiResponse<UserResponse>> verifyUser(
            @RequestHeader("X-User-Id") Long superUserId,
            @Valid @RequestBody UserVerificationRequest request) {
        log.info("REST: Verify user request received from SUPER_USER: {}", superUserId);
        
        UserResponse response = userService.verifyUser(String.valueOf(superUserId), request);
        
        return ResponseEntity.ok(ApiResponse.success(response, 
                "User verification completed: " + request.getStatus()));
    }

    /**
     * Get pending verification users
     * GET /api/users/pending-verification
     * Requires: SUPER_USER or ADMIN role
     */
    @GetMapping("/pending-verification")
    public ResponseEntity<ApiResponse<List<UserListResponse>>> getPendingVerificationUsers() {
        log.info("REST: Get pending verification users");
        
        List<UserListResponse> response = userService.getPendingVerificationUsers();
        
        return ResponseEntity.ok(ApiResponse.success(response, 
                "Pending verification users retrieved successfully"));
    }

    /**
     * Get user by ID
     * GET /api/users/{userId}
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @PathVariable @NotNull Long userId) {
        log.info("REST: Get user by ID: {}", userId);
        
        UserResponse response = userService.getUserById(userId);
        
        return ResponseEntity.ok(ApiResponse.success(response, "User retrieved successfully"));
    }

    /**
     * Update user information
     * PUT /api/users/{userId}
     */
    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable @NotNull String userId,
            @Valid @RequestBody UserUpdateRequest request) {
        log.info("REST: Update user request received for ID: {}", userId);
        
        UserResponse response = userService.updateUser(userId, request);
        
        return ResponseEntity.ok(ApiResponse.success(response, "User updated successfully"));
    }

    /**
     * Change user password
     * PUT /api/users/{userId}/password
     */
    @PutMapping("/{userId}/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @PathVariable @NotNull String userId,
            @Valid @RequestBody UserPasswordChangeRequest request) {
        log.info("REST: Change password request received for user: {}", userId);
        
        userService.changePassword(userId, request);
        
        return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully"));
    }

    /**
     * Assign roles to user by SUPER_USER
     * PUT /api/users/{userId}/roles
     * Requires: SUPER_USER role
     */
    @PutMapping("/{userId}/roles")
    public ResponseEntity<ApiResponse<UserResponse>> assignRoles(
            @RequestHeader("X-User-Id") Long superUserId,
            @PathVariable @NotNull Long userId,
            @RequestBody Set<String> roleNames) {
        log.info("REST: Assign roles request from SUPER_USER: {} to user: {}", superUserId, userId);
        
        UserResponse response = userService.assignRolesBySuperUser(superUserId, userId, roleNames);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Roles assigned successfully"));
    }

    /**
     * Assign authorities directly to user by SUPER_USER
     * PUT /api/users/authorities/assign
     * Requires: SUPER_USER role
     */
    @PutMapping("/authorities/assign")
    public ResponseEntity<ApiResponse<UserResponse>> assignAuthorities(
            @RequestHeader("X-User-Id") Long superUserId,
            @Valid @RequestBody UserAuthorityAssignmentRequest request) {
        log.info("REST: Assign authorities request from SUPER_USER: {}", superUserId);
        
        UserResponse response = userService.assignAuthoritiesToUser(superUserId, request);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Authorities assigned successfully"));
    }

    /**
     * Search users with filters
     * POST /api/users/search
     */
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<Page<UserListResponse>>> searchUsers(
            @Valid @RequestBody UserSearchCriteria criteria) {
        log.info("REST: Search users request received");
        
        Page<UserListResponse> response = userService.searchUsers(criteria);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Users retrieved successfully"));
    }
}