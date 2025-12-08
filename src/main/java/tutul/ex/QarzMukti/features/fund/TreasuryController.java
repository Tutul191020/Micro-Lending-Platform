package tutul.ex.QarzMukti.features.fund;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tutul.ex.QarzMukti.shared.dto.ApiResponse;
import tutul.ex.QarzMukti.shared.dto.FundAllocationRequest;
import tutul.ex.QarzMukti.shared.dto.FundPoolResponse;
import tutul.ex.QarzMukti.shared.dto.TreasuryMintRequest;
import tutul.ex.QarzMukti.shared.dto.TransactionLogResponse;
import tutul.ex.QarzMukti.shared.entities.User;
import tutul.ex.QarzMukti.shared.repositories.UserRepository;
import tutul.ex.QarzMukti.features.fund.TreasuryService;
import tutul.ex.QarzMukti.features.reconciliation.TransactionLogService;

/**
 * Controller for Treasury operations.
 * Handles minting funds and allocating funds to accountants.
 */
@Slf4j
@RestController
@RequestMapping("/api/treasury")
@RequiredArgsConstructor
public class TreasuryController {

    private final TreasuryService treasuryService;
    private final UserRepository userRepository;
    private final TransactionLogService transactionLogService;

    /**
     * Mint new funds into the treasury.
     * Requires FUND:CREATE authority.
     */
    @PostMapping("/mint")
    @PreAuthorize("hasAuthority('FUND:CREATE')")
    public ResponseEntity<ApiResponse<FundPoolResponse>> mintFund(
            @RequestBody TreasuryMintRequest request,
            Authentication authentication) {
        
        log.info("Mint fund request received: Amount={}", request.getAmount());
        User admin = getUserFromAuthentication(authentication);
        
        FundPoolResponse response = treasuryService.mintFund(request, admin);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Funds minted successfully"));
    }

    /**
     * Allocate funds from treasury to an accountant.
     * Requires FUND:ALLOCATE authority.
     */
    @PostMapping("/allocate")
    @PreAuthorize("hasAuthority('FUND:ALLOCATE')")
    public ResponseEntity<ApiResponse<FundPoolResponse>> allocateFund(
            @RequestBody FundAllocationRequest request,
            Authentication authentication) {
        
        log.info("Fund allocation request received: AccountantId={}, Amount={}", 
                request.getAccountantId(), request.getAmount());
        User admin = getUserFromAuthentication(authentication);
        
        FundPoolResponse response = treasuryService.allocateFundToAccountant(request, admin);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Funds allocated successfully"));
    }

    /**
     * Get the current user's fund pool balance.
     * Requires FUND:READ authority.
     */
    @GetMapping("/balance")
    @PreAuthorize("hasAuthority('FUND:READ')")
    public ResponseEntity<ApiResponse<FundPoolResponse>> getBalance(Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        
        FundPoolResponse response = treasuryService.getUserFundPool(user);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Fund pool retrieved successfully"));
    }


    private User getUserFromAuthentication(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found: " + authentication.getName()));
    }

    /**
     * Get transaction history for the current user.
     * Requires FUND:READ authority.
     */
    @GetMapping("/transactions")
    @PreAuthorize("hasAuthority('FUND:READ')")
    public ResponseEntity<ApiResponse<ApiResponse.PageResponse<TransactionLogResponse>>> getTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        
        User user = getUserFromAuthentication(authentication);
        Pageable pageable = PageRequest.of(page, size);
        
        Page<TransactionLogResponse> response = transactionLogService.getTransactionHistory(user.getId(), pageable);
        
        return ResponseEntity.ok(ApiResponse.successPage(response, "Transaction history retrieved successfully"));
    }

}
