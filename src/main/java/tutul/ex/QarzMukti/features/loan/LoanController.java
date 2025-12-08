package tutul.ex.QarzMukti.features.loan;

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
import tutul.ex.QarzMukti.shared.common.ResourceNotFoundException;
import tutul.ex.QarzMukti.shared.dto.*;
import tutul.ex.QarzMukti.shared.entities.User;
import tutul.ex.QarzMukti.shared.repositories.UserRepository;
import tutul.ex.QarzMukti.features.loan.CompleteLoanService;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
public class LoanController {

    private final CompleteLoanService loanService;
    private final UserRepository userRepository;

    @PostMapping("/apply")
    @PreAuthorize("hasAuthority('LOAN:CREATE')")
    public ResponseEntity<ApiResponse<LoanResponse>> applyForLoan(
            @Valid @RequestBody LoanApplicationRequest request,
            Authentication authentication) {

        log.info("Loan application request received: principal={}, tenure={}", 
                request.getPrincipal(), request.getTenureMonths());
        User user = getUserFromAuthentication(authentication);

        LoanResponse response = loanService.applyForLoan(request, user);

        return ResponseEntity.ok(ApiResponse.success(response, "Loan application submitted successfully"));
    }

    @PostMapping("/approve")
    @PreAuthorize("hasAuthority('LOAN:APPROVE') or hasAuthority('LOAN:REJECT')")
    public ResponseEntity<ApiResponse<LoanResponse>> approveLoan(
            @Valid @RequestBody LoanApprovalRequest request,
            Authentication authentication) {

        log.info("Loan approval request received: loanId={}, decision={}", 
                request.getLoanId(), request.getDecision());
        User accountant = getUserFromAuthentication(authentication);

        LoanResponse response = loanService.approveLoan(request, accountant);

        return ResponseEntity.ok(ApiResponse.success(response, 
                "Loan " + request.getDecision().toLowerCase() + " successfully"));
    }

    @GetMapping("/{loanId}")
    @PreAuthorize("hasAuthority('LOAN:READ')")
    public ResponseEntity<ApiResponse<LoanDetailsResponse>> getLoanDetails(
            @PathVariable UUID loanId,
            Authentication authentication) {

        log.debug("Fetching loan details: loanId={}", loanId);
        User user = getUserFromAuthentication(authentication);

        LoanDetailsResponse response = loanService.getLoanDetails(loanId, user);

        return ResponseEntity.ok(ApiResponse.success(response, "Loan details retrieved successfully"));
    }

    @GetMapping("/my-loans")
    @PreAuthorize("hasAuthority('LOAN:READ')")
    public ResponseEntity<ApiResponse<List<LoanResponse>>> getMyLoans(
            Authentication authentication) {

        log.debug("Fetching user loans");
        User user = getUserFromAuthentication(authentication);

        List<LoanResponse> response = loanService.getUserLoans(user);

        return ResponseEntity.ok(ApiResponse.success(response, "User loans retrieved successfully"));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAuthority('LOAN:READ')")
    public ResponseEntity<ApiResponse<ApiResponse.PageResponse<LoanResponse>>> getLoansByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.debug("Fetching loans by status: status={}", status);
        Pageable pageable = PageRequest.of(page, size);

        Page<LoanResponse> response = loanService.getLoansByStatus(status, pageable);

        return ResponseEntity.ok(ApiResponse.successPage(response, "Loans retrieved successfully"));
    }

    /**
     * Get all overdue loans (Admin/Accountant/SuperAdmin)
     */
    @GetMapping("/overdue")
    @PreAuthorize("hasAnyAuthority('LOAN:APPROVE', 'LOAN:READ_ALL', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<LoanResponse>>> getOverdueLoans() {
        List<LoanResponse> loans = loanService.getOverdueLoans();
        return ResponseEntity.ok(ApiResponse.success(loans, "Overdue loans retrieved successfully"));
    }

    private User getUserFromAuthentication(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + authentication.getName()));
    }
}
