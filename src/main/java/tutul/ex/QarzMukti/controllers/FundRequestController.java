package tutul.ex.QarzMukti.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tutul.ex.QarzMukti.dto.ApiResponse;
import tutul.ex.QarzMukti.dto.FundRequestCreateRequest;
import tutul.ex.QarzMukti.dto.FundRequestResponse;
import tutul.ex.QarzMukti.dto.FundRequestReviewRequest;
import tutul.ex.QarzMukti.entities.User;
import tutul.ex.QarzMukti.repositories.UserRepository;
import tutul.ex.QarzMukti.services.FundRequestService;

import java.util.UUID;

/**
 * Controller for fund request operations.
 * Handles accountant fund requests and super admin approvals.
 */
@Slf4j
@RestController
@RequestMapping("/api/fund-requests")
@RequiredArgsConstructor
public class FundRequestController {

    private final FundRequestService fundRequestService;
    private final UserRepository userRepository;

    /**
     * Create a fund request (Accountant).
     */
    @PostMapping
    @PreAuthorize("hasAuthority('FUND:READ')")
    public ResponseEntity<ApiResponse<FundRequestResponse>> createFundRequest(
            @RequestBody FundRequestCreateRequest request,
            Authentication authentication) {
        
        log.info("Fund request creation: Amount={}", request.getAmount());
        User accountant = getUserFromAuthentication(authentication);
        
        FundRequestResponse response = fundRequestService.createFundRequest(request, accountant);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Fund request created successfully"));
    }

    /**
     * Get my fund requests (Accountant).
     */
    @GetMapping("/my-requests")
    @PreAuthorize("hasAuthority('FUND:READ')")
    public ResponseEntity<ApiResponse<ApiResponse.PageResponse<FundRequestResponse>>> getMyRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        
        User accountant = getUserFromAuthentication(authentication);
        Pageable pageable = PageRequest.of(page, size);
        
        Page<FundRequestResponse> response = fundRequestService.getMyRequests(accountant, pageable);
        
        return ResponseEntity.ok(ApiResponse.successPage(response, "Fund requests retrieved successfully"));
    }

    /**
     * Get a specific fund request by ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('FUND:READ')")
    public ResponseEntity<ApiResponse<FundRequestResponse>> getFundRequest(@PathVariable UUID id) {
        FundRequestResponse response = fundRequestService.getFundRequestById(id);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Fund request retrieved successfully"));
    }

    /**
     * Get pending fund requests (Super Admin).
     */
    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('FUND:ALLOCATE')")
    public ResponseEntity<ApiResponse<ApiResponse.PageResponse<FundRequestResponse>>> getPendingRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<FundRequestResponse> response = fundRequestService.getPendingRequests(pageable);
        
        return ResponseEntity.ok(ApiResponse.successPage(response, "Pending fund requests retrieved successfully"));
    }

    /**
     * Approve a fund request (Super Admin).
     */
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('FUND:ALLOCATE')")
    public ResponseEntity<ApiResponse<FundRequestResponse>> approveFundRequest(
            @PathVariable UUID id,
            @RequestBody(required = false) FundRequestReviewRequest request,
            Authentication authentication) {
        
        log.info("Approving fund request: ID={}", id);
        User admin = getUserFromAuthentication(authentication);
        
        String comments = request != null ? request.getComments() : null;
        FundRequestResponse response = fundRequestService.approveFundRequest(id, admin, comments);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Fund request approved and funds allocated successfully"));
    }

    /**
     * Reject a fund request (Super Admin).
     */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('FUND:ALLOCATE')")
    public ResponseEntity<ApiResponse<FundRequestResponse>> rejectFundRequest(
            @PathVariable UUID id,
            @RequestBody(required = false) FundRequestReviewRequest request,
            Authentication authentication) {
        
        log.info("Rejecting fund request: ID={}", id);
        User admin = getUserFromAuthentication(authentication);
        
        String comments = request != null ? request.getComments() : null;
        FundRequestResponse response = fundRequestService.rejectFundRequest(id, admin, comments);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Fund request rejected"));
    }

    private User getUserFromAuthentication(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found: " + authentication.getName()));
    }
}
