package tutul.ex.QarzMukti.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tutul.ex.QarzMukti.constants.ApiEndpoints;
import tutul.ex.QarzMukti.dto.*;
import tutul.ex.QarzMukti.services.UserService;

import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    @PostMapping(ApiEndpoints.Users.DOCUMENTS_UPLOAD)
    public ResponseEntity<ApiResponse<DocumentUploadResponse>> uploadDocument(
            @PathVariable String userId,
            @RequestParam("documentType") String documentType,
            @RequestParam("file") MultipartFile file) {
        log.info("Document upload request - userId: {}, type: {}, fileName: {}", userId, documentType,
                file.getOriginalFilename());
        DocumentUploadResponse response = userService.uploadDocument(userId, documentType, file);
        log.info("Document uploaded successfully for user: {}", userId);
        return new ResponseEntity<>(ApiResponse.success(response, "Document uploaded successfully"), HttpStatus.OK);
    }

    @PutMapping(ApiEndpoints.Users.VERIFY)
    public ResponseEntity<ApiResponse<UserResponse>> verifyUser(
            @RequestHeader("X-User-Id") Long superUserId,
            @Valid @RequestBody UserVerificationRequest request) {
        log.info("User verification request - superUserId: {}, targetUserId: {}, status: {}", superUserId,
                request.getUserId(), request.getStatus());
        UserResponse response = userService.verifyUser(String.valueOf(superUserId), request);
        log.info("User {} verification completed with status: {}", request.getUserId(), request.getStatus());
        return new ResponseEntity<>(
                ApiResponse.success(response, "User verification completed: " + request.getStatus()), HttpStatus.OK);
    }

    @GetMapping(ApiEndpoints.Users.PENDING_VERIFICATION)
    public ResponseEntity<ApiResponse<List<UserListResponse>>> getPendingVerificationUsers() {
        log.info("Fetching pending verification users");
        List<UserListResponse> response = userService.getPendingVerificationUsers();
        log.info("Found {} users pending verification", response.size());
        return new ResponseEntity<>(ApiResponse.success(response, "Pending verification users retrieved successfully"),
                HttpStatus.OK);
    }

    @GetMapping(ApiEndpoints.Users.BY_ID)
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long userId) {
        log.info("Fetching user details for userId: {}", userId);
        UserResponse response = userService.getUserById(userId);
        return new ResponseEntity<>(ApiResponse.success(response, "User retrieved successfully"), HttpStatus.OK);
    }

    @PutMapping(ApiEndpoints.Users.BY_ID)
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable String userId,
            @Valid @RequestBody UserUpdateRequest request) {
        log.info("Updating user details for userId: {}", userId);
        UserResponse response = userService.updateUser(userId, request);
        log.info("User {} updated successfully", userId);
        return new ResponseEntity<>(ApiResponse.success(response, "User updated successfully"), HttpStatus.OK);
    }

    @PutMapping(ApiEndpoints.Users.UPDATE_PASSWORD)
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @PathVariable String userId,
            @Valid @RequestBody UserPasswordChangeRequest request) {
        log.info("Password change request for userId: {}", userId);
        userService.changePassword(userId, request);
        log.info("Password changed successfully for userId: {}", userId);
        return new ResponseEntity<>(ApiResponse.success(null, "Password changed successfully"), HttpStatus.OK);
    }

    @PutMapping(ApiEndpoints.Users.ASSIGN_ROLES)
    public ResponseEntity<ApiResponse<UserResponse>> assignRoles(
            @RequestHeader("X-User-Id") Long superUserId,
            @PathVariable Long userId,
            @RequestBody Set<String> roleNames) {
        log.info("Role assignment request - superUserId: {}, targetUserId: {}, roles: {}", superUserId, userId,
                roleNames);
        UserResponse response = userService.assignRolesBySuperUser(superUserId, userId, roleNames);
        log.info("Roles assigned successfully to userId: {}", userId);
        return new ResponseEntity<>(ApiResponse.success(response, "Roles assigned successfully"), HttpStatus.OK);
    }

    @PutMapping(ApiEndpoints.Users.ASSIGN_AUTHORITIES)
    public ResponseEntity<ApiResponse<UserResponse>> assignAuthorities(
            @RequestHeader("X-User-Id") Long superUserId,
            @Valid @RequestBody UserAuthorityAssignmentRequest request) {
        log.info("Authority assignment request - superUserId: {}, targetUserId: {}", superUserId, request.getUserId());
        UserResponse response = userService.assignAuthoritiesToUser(superUserId, request);
        log.info("Authorities assigned successfully to userId: {}", request.getUserId());
        return new ResponseEntity<>(ApiResponse.success(response, "Authorities assigned successfully"), HttpStatus.OK);
    }

    @PostMapping(ApiEndpoints.Users.SEARCH)
    public ResponseEntity<ApiResponse<Page<UserListResponse>>> searchUsers(
            @Valid @RequestBody UserSearchCriteria criteria) {
        log.info("User search request with criteria - page: {}, size: {}", criteria.getPage(), criteria.getSize());
        Page<UserListResponse> response = userService.searchUsers(criteria);
        log.info("User search completed - found {} users", response.getTotalElements());
        return new ResponseEntity<>(ApiResponse.success(response, "Users retrieved successfully"), HttpStatus.OK);
    }
}