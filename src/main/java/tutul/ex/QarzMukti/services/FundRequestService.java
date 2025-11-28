package tutul.ex.QarzMukti.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tutul.ex.QarzMukti.common.ValidationException;
import tutul.ex.QarzMukti.dto.FundAllocationRequest;
import tutul.ex.QarzMukti.dto.FundRequestCreateRequest;
import tutul.ex.QarzMukti.dto.FundRequestResponse;
import tutul.ex.QarzMukti.dto.FundRequestReviewRequest;
import tutul.ex.QarzMukti.entities.FundRequest;
import tutul.ex.QarzMukti.entities.User;
import tutul.ex.QarzMukti.repositories.FundRequestRepository;
import tutul.ex.QarzMukti.repositories.UserRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for managing fund requests from accountants.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FundRequestService {

    private final FundRequestRepository fundRequestRepository;
    private final UserRepository userRepository;
    private final TreasuryService treasuryService;
    private final TransactionLogService transactionLogService;

    /**
     * Create a fund request from an accountant.
     */
    @Transactional
    public FundRequestResponse createFundRequest(FundRequestCreateRequest request, User accountant) {
        log.info("Creating fund request: Accountant={}, Amount={}", accountant.getUsername(), request.getAmount());

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Request amount must be positive");
        }

        if (request.getReason() == null || request.getReason().trim().isEmpty()) {
            throw new ValidationException("Request reason is required");
        }

        FundRequest fundRequest = FundRequest.builder()
                .requestedBy(accountant)
                .amount(request.getAmount())
                .reason(request.getReason())
                .status("PENDING")
                .build();

        fundRequest = fundRequestRepository.save(fundRequest);

        // Log the request creation
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("requestId", fundRequest.getId().toString());
        metadata.put("reason", request.getReason());

        transactionLogService.logTransaction(
                "FUND_REQUEST_CREATED",
                request.getAmount(),
                null,
                accountant,
                null,
                null,
                fundRequest.getId(),
                "FUND_REQUEST",
                "Fund request created by " + accountant.getUsername(),
                metadata,
                accountant
        );

        log.info("Fund request created successfully: ID={}", fundRequest.getId());
        return mapToResponse(fundRequest);
    }

    /**
     * Get pending fund requests.
     */
    public Page<FundRequestResponse> getPendingRequests(Pageable pageable) {
        Page<FundRequest> requests = fundRequestRepository.findByStatus("PENDING", pageable);
        return requests.map(this::mapToResponse);
    }

    /**
     * Get all fund requests for a specific accountant.
     */
    public Page<FundRequestResponse> getMyRequests(User accountant, Pageable pageable) {
        Page<FundRequest> requests = fundRequestRepository.findByRequestedById(accountant.getId(), pageable);
        return requests.map(this::mapToResponse);
    }

    /**
     * Get a specific fund request by ID.
     */
    public FundRequestResponse getFundRequestById(UUID requestId) {
        FundRequest fundRequest = fundRequestRepository.findById(requestId)
                .orElseThrow(() -> new ValidationException("Fund request not found: " + requestId));
        return mapToResponse(fundRequest);
    }

    /**
     * Approve a fund request and allocate funds.
     */
    @Transactional
    public FundRequestResponse approveFundRequest(UUID requestId, User admin, String comments) {
        log.info("Approving fund request: ID={}, Admin={}", requestId, admin.getUsername());

        FundRequest fundRequest = fundRequestRepository.findById(requestId)
                .orElseThrow(() -> new ValidationException("Fund request not found: " + requestId));

        if (!"PENDING".equals(fundRequest.getStatus())) {
            throw new ValidationException("Fund request is not pending. Current status: " + fundRequest.getStatus());
        }

        // Update fund request
        fundRequest.setStatus("APPROVED");
        fundRequest.setReviewedBy(admin);
        fundRequest.setReviewedAt(Instant.now());
        fundRequest.setReviewComments(comments);
        fundRequest = fundRequestRepository.save(fundRequest);

        // Allocate funds using TreasuryService
        FundAllocationRequest allocationRequest = FundAllocationRequest.builder()
                .accountantId(fundRequest.getRequestedBy().getId())
                .amount(fundRequest.getAmount())
                .build();

        treasuryService.allocateFundToAccountant(allocationRequest, admin);

        // Log the approval
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("requestId", fundRequest.getId().toString());
        metadata.put("comments", comments != null ? comments : "");

        transactionLogService.logTransaction(
                "FUND_REQUEST_APPROVED",
                fundRequest.getAmount(),
                admin,
                fundRequest.getRequestedBy(),
                null,
                null,
                fundRequest.getId(),
                "FUND_REQUEST",
                "Fund request approved and funds allocated",
                metadata,
                admin
        );

        log.info("Fund request approved successfully: ID={}", requestId);
        return mapToResponse(fundRequest);
    }

    /**
     * Reject a fund request.
     */
    @Transactional
    public FundRequestResponse rejectFundRequest(UUID requestId, User admin, String comments) {
        log.info("Rejecting fund request: ID={}, Admin={}", requestId, admin.getUsername());

        FundRequest fundRequest = fundRequestRepository.findById(requestId)
                .orElseThrow(() -> new ValidationException("Fund request not found: " + requestId));

        if (!"PENDING".equals(fundRequest.getStatus())) {
            throw new ValidationException("Fund request is not pending. Current status: " + fundRequest.getStatus());
        }

        fundRequest.setStatus("REJECTED");
        fundRequest.setReviewedBy(admin);
        fundRequest.setReviewedAt(Instant.now());
        fundRequest.setReviewComments(comments);
        fundRequest = fundRequestRepository.save(fundRequest);

        // Log the rejection
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("requestId", fundRequest.getId().toString());
        metadata.put("comments", comments != null ? comments : "");

        transactionLogService.logTransaction(
                "FUND_REQUEST_REJECTED",
                fundRequest.getAmount(),
                null,
                null,
                null,
                null,
                fundRequest.getId(),
                "FUND_REQUEST",
                "Fund request rejected by " + admin.getUsername(),
                metadata,
                admin
        );

        log.info("Fund request rejected: ID={}", requestId);
        return mapToResponse(fundRequest);
    }

    private FundRequestResponse mapToResponse(FundRequest fundRequest) {
        return FundRequestResponse.builder()
                .id(fundRequest.getId())
                .amount(fundRequest.getAmount())
                .reason(fundRequest.getReason())
                .status(fundRequest.getStatus())
                .requestedById(fundRequest.getRequestedBy().getId())
                .requestedByFullName(fundRequest.getRequestedBy().getFullName())
                .reviewedById(fundRequest.getReviewedBy() != null ? fundRequest.getReviewedBy().getId() : null)
                .reviewedByUsername(fundRequest.getReviewedBy() != null ? fundRequest.getReviewedBy().getUsername() : null)
                .reviewedAt(fundRequest.getReviewedAt())
                .reviewComments(fundRequest.getReviewComments())
                .createdAt(fundRequest.getCreatedAt())
                .build();
    }
}
