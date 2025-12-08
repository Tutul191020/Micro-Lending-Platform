package tutul.ex.QarzMukti.features.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tutul.ex.QarzMukti.shared.constants.ApiEndpoints;
import tutul.ex.QarzMukti.shared.dto.*;
import tutul.ex.QarzMukti.features.user.UserService;

import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping(ApiEndpoints.Users.BASE)
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
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('USER:VERIFY')")
    public ResponseEntity<ApiResponse<UserResponse>> verifyUser(
            @Valid @RequestBody UserVerificationRequest request) {
        log.info("User verification request - targetUserId: {}, status: {}",
                request.getUserId(), request.getStatus());
        UserResponse response = userService.verifyUser(request);
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
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('USER:ASSIGN_ROLE')")
    public ResponseEntity<ApiResponse<UserResponse>> assignRoles(
            @PathVariable Long userId,
            @RequestBody Set<String> roleNames) {
        log.info("Role assignment request - targetUserId: {}, roles: {}", userId,
                roleNames);
        UserResponse response = userService.assignRolesBySuperUser(userId, roleNames);
        log.info("Roles assigned successfully to userId: {}", userId);
        return new ResponseEntity<>(ApiResponse.success(response, "Roles assigned successfully"), HttpStatus.OK);
    }

    @PutMapping(ApiEndpoints.Users.ASSIGN_AUTHORITIES)
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('USER:ASSIGN_ROLE')")
    public ResponseEntity<ApiResponse<UserResponse>> assignAuthorities(
            @Valid @RequestBody UserAuthorityAssignmentRequest request) {
        log.info("Authority assignment request - targetUserId: {}", request.getUserId());
        UserResponse response = userService.assignAuthoritiesToUser(request);
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

    @GetMapping(ApiEndpoints.Users.ONBOARDING_STATUS)
    public ResponseEntity<ApiResponse<OnboardingStatusResponse>> getOnboardingStatus(@PathVariable Long userId) {
        log.info("Fetching onboarding status for userId: {}", userId);
        OnboardingStatusResponse response = userService.getOnboardingStatus(userId);
        log.info("Onboarding status retrieved for userId: {} - Status: {}, Completion: {}%", 
                userId, response.getOnboardingStatus(), response.getDocumentStatus().getCompletionPercentage());
        return new ResponseEntity<>(ApiResponse.success(response, "Onboarding status retrieved successfully"), HttpStatus.OK);
    }

    @PostMapping(ApiEndpoints.Users.COMPLETE_ONBOARDING)
    public ResponseEntity<ApiResponse<UserResponse>> completeOnboarding(@PathVariable Long userId) {
        log.info("Completing onboarding for userId: {}", userId);
        UserResponse response = userService.completeOnboarding(userId);
        log.info("Onboarding completed successfully for userId: {}", userId);
        return new ResponseEntity<>(ApiResponse.success(response, "Onboarding completed successfully"), HttpStatus.OK);
    }

    @GetMapping(ApiEndpoints.Users.LOAN_ELIGIBILITY)
    public ResponseEntity<ApiResponse<LoanEligibilityResponse>> getLoanEligibility(@PathVariable Long userId) {
        log.info("Checking loan eligibility for userId: {}", userId);
        LoanEligibilityResponse response = userService.getLoanEligibility(userId);
        log.info("Loan eligibility checked for userId: {} - Eligible: {}", userId, response.getIsEligible());
        return new ResponseEntity<>(ApiResponse.success(response, "Loan eligibility checked successfully"), HttpStatus.OK);
    }
}