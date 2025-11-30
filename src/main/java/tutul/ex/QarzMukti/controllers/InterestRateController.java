package tutul.ex.QarzMukti.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tutul.ex.QarzMukti.dto.*;
import tutul.ex.QarzMukti.entities.User;
import tutul.ex.QarzMukti.repositories.UserRepository;
import tutul.ex.QarzMukti.services.InterestRateService;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/interest-rates")
@RequiredArgsConstructor
public class InterestRateController {

    private final InterestRateService interestRateService;
    private final UserRepository userRepository;
    private final tutul.ex.QarzMukti.services.CompleteLoanService completeLoanService;

    /**
     * Set or update the system-wide default interest rate
     * SuperAdmin only - requires INTEREST:UPDATE authority
     */
    @PostMapping("/system")
    @PreAuthorize("hasAuthority('INTEREST:UPDATE')")
    public ResponseEntity<ApiResponse<SystemInterestRateResponse>> setSystemInterestRate(
            @Valid @RequestBody SystemInterestRateRequest request,
            Authentication authentication) {

        log.info("Setting system interest rate: {}%", request.getInterestRate());
        User admin = getUserFromAuthentication(authentication);

        SystemInterestRateResponse response = interestRateService.setSystemInterestRate(request, admin);

        return ResponseEntity.ok(ApiResponse.success(response, "System interest rate updated successfully"));
    }

    /**
     * Get the current active system interest rate
     * All authenticated users can view this
     */
    @GetMapping("/system/current")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SystemInterestRateResponse>> getCurrentSystemInterestRate() {

        log.debug("Fetching current system interest rate");
        SystemInterestRateResponse response = interestRateService.getCurrentSystemInterestRate();

        return ResponseEntity.ok(ApiResponse.success(response, "Current interest rate retrieved successfully"));
    }

    /**
     * Get history of system interest rate changes
     * SuperAdmin only - requires INTEREST:READ authority
     */
    @GetMapping("/system/history")
    @PreAuthorize("hasAuthority('INTEREST:READ')")
    public ResponseEntity<ApiResponse<ApiResponse.PageResponse<SystemInterestRateResponse>>> getSystemInterestRateHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.debug("Fetching system interest rate history");
        Pageable pageable = PageRequest.of(page, size);

        Page<SystemInterestRateResponse> response = interestRateService.getSystemInterestRateHistory(pageable);

        return ResponseEntity.ok(ApiResponse.successPage(response, "Interest rate history retrieved successfully"));
    }

    /**
     * Override interest rate for a specific loan
     * SuperAdmin only - requires INTEREST:OVERRIDE authority
     */
    @PostMapping("/loan/override")
    @PreAuthorize("hasAuthority('INTEREST:OVERRIDE')")
    public ResponseEntity<ApiResponse<InterestRateAuditResponse>> overrideLoanInterestRate(
            @Valid @RequestBody LoanInterestRateOverrideRequest request,
            Authentication authentication) {

        log.info("Overriding interest rate for loan {}: {}%", request.getLoanId(), request.getInterestRate());
        User admin = getUserFromAuthentication(authentication);

        InterestRateAuditResponse response = interestRateService.overrideLoanInterestRate(request, admin);

        return ResponseEntity.ok(ApiResponse.success(response, "Loan interest rate overridden successfully"));
    }

    /**
     * Get interest rate change history for a specific loan
     * Requires INTEREST:READ authority
     */
    @GetMapping("/loan/{loanId}/history")
    @PreAuthorize("hasAuthority('INTEREST:READ')")
    public ResponseEntity<ApiResponse<ApiResponse.PageResponse<InterestRateAuditResponse>>> getLoanInterestRateHistory(
            @PathVariable UUID loanId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.debug("Fetching interest rate history for loan: {}", loanId);
        Pageable pageable = PageRequest.of(page, size);

        Page<InterestRateAuditResponse> response = interestRateService.getLoanInterestRateHistory(loanId, pageable);

        return ResponseEntity.ok(ApiResponse.successPage(response, "Loan interest rate history retrieved successfully"));
    }

    /**
     * Get all interest rate audit records
     * SuperAdmin only - requires INTEREST:READ authority
     */
    @GetMapping("/audit")
    @PreAuthorize("hasAuthority('INTEREST:READ')")
    public ResponseEntity<ApiResponse<ApiResponse.PageResponse<InterestRateAuditResponse>>> getAllInterestRateAudits(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.debug("Fetching all interest rate audit records");
        Pageable pageable = PageRequest.of(page, size);

        Page<InterestRateAuditResponse> response = interestRateService.getAllInterestRateAudits(pageable);

        return ResponseEntity.ok(ApiResponse.successPage(response, "Interest rate audit records retrieved successfully"));
    }

    /**
     * Recalculate loan repayment schedule with new interest rate
     * SuperAdmin only - requires INTEREST:OVERRIDE authority
     */
    @PostMapping("/loan/recalculate")
    @PreAuthorize("hasAuthority('INTEREST:OVERRIDE')")
    public ResponseEntity<ApiResponse<LoanRecalculationResponse>> recalculateLoanSchedule(
            @Valid @RequestBody LoanRecalculationRequest request,
            Authentication authentication) {

        log.info("Recalculating loan schedule for loan: {}", request.getLoanId());
        User admin = getUserFromAuthentication(authentication);

        LoanRecalculationResponse response = completeLoanService.recalculateLoanSchedule(
                request.getLoanId(),
                request.getNewInterestRate(),
                request.getReason(),
                admin
        );

        return ResponseEntity.ok(ApiResponse.success(response, "Loan schedule recalculated successfully"));
    }

    /**
     * Helper method to get User from Authentication
     */
    private User getUserFromAuthentication(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found: " + authentication.getName()));
    }
}
