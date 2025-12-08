package tutul.ex.QarzMukti.features.loan;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tutul.ex.QarzMukti.shared.common.ResourceNotFoundException;
import tutul.ex.QarzMukti.shared.common.ValidationException;
import tutul.ex.QarzMukti.shared.dto.*;
import tutul.ex.QarzMukti.shared.entities.*;
import tutul.ex.QarzMukti.shared.enums.InterestType;
import tutul.ex.QarzMukti.shared.enums.LoanStatus;
import tutul.ex.QarzMukti.shared.repositories.*;
import tutul.ex.QarzMukti.features.reconciliation.LedgerService;
import tutul.ex.QarzMukti.features.reconciliation.TransactionLogService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Comprehensive Loan Service handling complete loan lifecycle:
 * - Loan application
 * - Loan approval/rejection
 * - Loan disbursement with ledger integration
 * - Repayment schedule generation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CompleteLoanService {

        private final LoanRepository loanRepository;
        private final LoanApprovalRepository loanApprovalRepository;
        private final LoanRepaymentRepository loanRepaymentRepository;
        private final UserRepository userRepository;
        private final VirtualFundPoolRepository virtualFundPoolRepository;
        private final LedgerService ledgerService;
        private final InterestRateService interestRateService;
        private final TransactionLogService transactionLogService;
        private final InterestRateAuditRepository interestRateAuditRepository;

        // ==================== LOAN APPLICATION ====================

        @Transactional
        public LoanResponse applyForLoan(LoanApplicationRequest request, User user) {
                log.info("Processing loan application for user {}: amount={}, tenure={}",
                                user.getUsername(), request.getPrincipal(), request.getTenureMonths());

                // Validate user eligibility
                validateUserEligibility(user);

                // Get current system interest rate
                BigDecimal systemInterestRate = interestRateService.getCurrentInterestRateValue();

                // Create loan entity
                Loan loan = new Loan();
                loan.setId(UUID.randomUUID());
                loan.setUser(user);
                loan.setPrincipal(request.getPrincipal());
                loan.setApprovedInterestRate(systemInterestRate); // Default, can be overridden
                loan.setInterestType(request.getInterestType().name());
                loan.setTenureMonths(request.getTenureMonths());
                loan.setStatus(LoanStatus.PENDING_APPROVAL.name());
                loan.setOutstandingAmount(request.getPrincipal());
                loan.setCurrency("BDT");
                loan.setVersion(0L);
                loan.setCreatedAt(Instant.now());

                Loan savedLoan = loanRepository.save(loan);

                // Assign to an accountant (simple round-robin or first available)
                User assignedAccountant = assignAccountant();

                // Create loan approval record
                LoanApproval approval = new LoanApproval();
                approval.setId(UUID.randomUUID());
                approval.setLoan(savedLoan);
                approval.setAssignedAccountant(assignedAccountant);
                approval.setProposedInterestRate(systemInterestRate);
                approval.setStatus("PENDING");
                approval.setCreatedAt(Instant.now());

                loanApprovalRepository.save(approval);

                log.info("Loan application created successfully: loanId={}, assigned to accountant={}",
                                savedLoan.getId(), assignedAccountant.getUsername());

                return mapToLoanResponse(savedLoan);
        }

        // ==================== LOAN APPROVAL/REJECTION ====================

        @Transactional
        public LoanResponse approveLoan(LoanApprovalRequest request, User accountant) {
                log.info("Processing loan decision by accountant {}: loanId={}, decision={}",
                                accountant.getUsername(), request.getLoanId(), request.getDecision());

                // Find loan with pessimistic lock
                Loan loan = loanRepository.findByIdForUpdate(request.getLoanId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Loan not found: " + request.getLoanId()));

                // Validate loan status
                if (!LoanStatus.PENDING_APPROVAL.name().equals(loan.getStatus())) {
                        throw new ValidationException(
                                        "Only PENDING_APPROVAL status can permissible: " + loan.getStatus());
                }

                // Find approval record
                LoanApproval approval = loanApprovalRepository.findByLoan(loan)
                                .orElseThrow(() -> new ResourceNotFoundException("Loan approval record not found"));

                if ("APPROVED".equalsIgnoreCase(request.getDecision())) {
                        return processApproval(loan, approval, request, accountant);
                } else if ("REJECTED".equalsIgnoreCase(request.getDecision())) {
                        return processRejection(loan, approval, request, accountant);
                } else {
                        throw new ValidationException("Invalid decision. Must be APPROVED or REJECTED");
                }
        }

        @Transactional
        protected LoanResponse processApproval(Loan loan, LoanApproval approval,
                        LoanApprovalRequest request, User accountant) {
                log.info("Approving loan: loanId={}", loan.getId());

                // Override interest rate if provided
                if (request.getOverrideInterestRate() != null) {
                        loan.setApprovedInterestRate(request.getOverrideInterestRate());
                        log.info("Interest rate overridden to {}%", request.getOverrideInterestRate());
                }

                // Validate accountant has sufficient funds
                VirtualFundPool accountantPool = virtualFundPoolRepository.findByUserId(accountant.getId())
                                .orElseThrow(() -> new ValidationException(
                                                "Accountant has not created any fund pool request yet"));

                if (accountantPool.getBalance().compareTo(loan.getPrincipal()) < 0) {
                        throw new ValidationException("Insufficient funds in accountant pool. Required: " +
                                        loan.getPrincipal() + " Available: " + accountantPool.getBalance());
                }

                // Disburse loan (deduct from accountant, create ledger entries)
                disburseLoan(loan, accountant, accountantPool);

                // Update loan status
                loan.setStatus(LoanStatus.DISBURSED.name());
                loan.setApprovedBy(accountant);
                loan.setApprovedAt(Instant.now());
                loan.setDisbursedAt(Instant.now());
                loan.setVersion(loan.getVersion() + 1);

                // Update approval record
                approval.setStatus("APPROVED");
                approval.setDecisionComment(request.getComment());
                approval.setDecidedAt(Instant.now());

                loanRepository.save(loan);
                loanApprovalRepository.save(approval);

                // Generate repayment schedule
                generateRepaymentSchedule(loan);

                log.info("Loan approved and disbursed successfully: loanId={}", loan.getId());

                return mapToLoanResponse(loan);
        }

        private LoanResponse processRejection(Loan loan, LoanApproval approval,
                        LoanApprovalRequest request, User accountant) {
                log.info("Rejecting loan: loanId={}", loan.getId());

                // Update loan status
                loan.setStatus(LoanStatus.REJECTED.name());
                loan.setApprovedBy(accountant);
                loan.setApprovedAt(Instant.now());
                loan.setVersion(loan.getVersion() + 1);

                // Update approval record
                approval.setStatus("REJECTED");
                approval.setDecisionComment(request.getComment());
                approval.setDecidedAt(Instant.now());

                loanRepository.save(loan);
                loanApprovalRepository.save(approval);

                log.info("Loan rejected: loanId={}", loan.getId());

                return mapToLoanResponse(loan);
        }

        // ==================== LOAN DISBURSEMENT ====================

        private void disburseLoan(Loan loan, User accountant, VirtualFundPool accountantPool) {
                log.info("Disbursing loan: loanId={}, amount={}", loan.getId(), loan.getPrincipal());

                // Deduct from accountant's fund pool
                BigDecimal newBalance = accountantPool.getBalance().subtract(loan.getPrincipal());
                accountantPool.setBalance(newBalance);
                accountantPool.setUpdatedAt(Instant.now());
                accountantPool.setVersion(accountantPool.getVersion() + 1);
                virtualFundPoolRepository.save(accountantPool);

                // Create ledger entry (debit accountant pool, credit user)
                ledgerService.createLoanDisbursementEntry(
                                accountantPool,
                                null, // User doesn't have a fund pool, money goes directly to them
                                loan.getPrincipal(),
                                loan.getId(),
                                accountant);

                // Create transaction log
                transactionLogService.logTransaction(
                                "LOAN_DISBURSEMENT",
                                loan.getPrincipal(),
                                accountant,
                                loan.getUser(),
                                accountantPool.getId(),
                                null,
                                loan.getId(),
                                "LOAN",
                                "Loan disbursed to " + loan.getUser().getFullName(),
                                java.util.Map.of("loanId", loan.getId().toString()),
                                accountant);

                log.info("Loan disbursed successfully: loanId={}, newAccountantBalance={}",
                                loan.getId(), newBalance);
        }

        // ==================== REPAYMENT SCHEDULE GENERATION ====================

        @Transactional
        public void generateRepaymentSchedule(Loan loan) {
                log.info("Generating repayment schedule for loan: loanId={}, tenure={}, interestType={}",
                                loan.getId(), loan.getTenureMonths(), loan.getInterestType());

                List<LoanRepayment> repayments = new ArrayList<>();
                BigDecimal principal = loan.getPrincipal();
                BigDecimal annualRate = loan.getApprovedInterestRate();
                int tenure = loan.getTenureMonths();

                if (InterestType.FLAT.name().equals(loan.getInterestType())) {
                        repayments = generateFlatInterestSchedule(loan, principal, annualRate, tenure);
                } else if (InterestType.REDUCING_BALANCE.name().equals(loan.getInterestType())) {
                        repayments = generateReducingBalanceSchedule(loan, principal, annualRate, tenure);
                } else {
                        // Default to FLAT
                        repayments = generateFlatInterestSchedule(loan, principal, annualRate, tenure);
                }

                List<LoanRepayment> savedRepayments = loanRepaymentRepository.saveAll(repayments);

                // Update loan outstanding amount
                BigDecimal totalRepayable = savedRepayments.stream()
                                .map(LoanRepayment::getTotalDue)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                loan.setOutstandingAmount(totalRepayable);
                loanRepository.save(loan);

                log.info("Repayment schedule generated: loanId={}, installments={}, totalRepayable={}",
                                loan.getId(), savedRepayments.size(), totalRepayable);
        }

        private List<LoanRepayment> generateFlatInterestSchedule(Loan loan, BigDecimal principal,
                        BigDecimal annualRate, int tenure) {
                // Flat interest: Total Interest = Principal × Rate × Time
                BigDecimal totalInterest = principal
                                .multiply(annualRate.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP))
                                .multiply(BigDecimal.valueOf(tenure).divide(BigDecimal.valueOf(12), 10,
                                                RoundingMode.HALF_UP));

                BigDecimal totalRepayable = principal.add(totalInterest);
                BigDecimal emiAmount = totalRepayable.divide(BigDecimal.valueOf(tenure), 2, RoundingMode.HALF_UP);

                BigDecimal principalPerMonth = principal.divide(BigDecimal.valueOf(tenure), 2, RoundingMode.HALF_UP);
                BigDecimal interestPerMonth = totalInterest.divide(BigDecimal.valueOf(tenure), 2, RoundingMode.HALF_UP);

                List<LoanRepayment> repayments = new ArrayList<>();
                LocalDate currentDueDate = LocalDate.now().plusMonths(1);

                for (int i = 0; i < tenure; i++) {
                        loanRepaymentResponse(loan, emiAmount, repayments, currentDueDate, interestPerMonth,
                                        principalPerMonth);
                        currentDueDate = currentDueDate.plusMonths(1);
                }

                return repayments;
        }

        private List<LoanRepayment> generateReducingBalanceSchedule(Loan loan, BigDecimal principal,
                        BigDecimal annualRate, int tenure) {
                // Reducing balance: EMI = [P × r × (1+r)^n] / [(1+r)^n - 1]
                BigDecimal monthlyRate = annualRate
                                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
                                .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);

                BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
                BigDecimal onePlusRPowerN = onePlusR.pow(tenure);

                BigDecimal emi = principal
                                .multiply(monthlyRate)
                                .multiply(onePlusRPowerN)
                                .divide(onePlusRPowerN.subtract(BigDecimal.ONE), 2, RoundingMode.HALF_UP);

                List<LoanRepayment> repayments = new ArrayList<>();
                BigDecimal outstandingPrincipal = principal;
                LocalDate currentDueDate = LocalDate.now().plusMonths(1);

                for (int i = 0; i < tenure; i++) {
                        BigDecimal interestDue = outstandingPrincipal
                                        .multiply(monthlyRate)
                                        .setScale(2, RoundingMode.HALF_UP);

                        BigDecimal principalDue = emi.subtract(interestDue);

                        // Last installment adjustment
                        if (i == tenure - 1) {
                                principalDue = outstandingPrincipal;
                                emi = principalDue.add(interestDue);
                        }

                        loanRepaymentResponse(loan, emi, repayments, currentDueDate, interestDue, principalDue);
                        outstandingPrincipal = outstandingPrincipal.subtract(principalDue);
                        currentDueDate = currentDueDate.plusMonths(1);
                }

                return repayments;
        }

        // ==================== LOAN RECALCULATION ====================

        /**
         * Recalculate loan repayment schedule with new interest rate
         * This is used for mid-loan interest rate adjustments by SuperAdmin
         * 
         * @param loanId          The loan to recalculate
         * @param newInterestRate The new interest rate to apply
         * @param reason          Reason for the rate change
         * @param admin           The SuperAdmin performing the change
         * @return Response with old vs new comparison
         */
        @Transactional
        public LoanRecalculationResponse recalculateLoanSchedule(UUID loanId, BigDecimal newInterestRate,
                        String reason, User admin) {
                log.info("Recalculating loan schedule: loanId={}, newRate={}%, reason={}",
                                loanId, newInterestRate, reason);

                // Find loan with pessimistic lock
                Loan loan = loanRepository.findByIdForUpdate(loanId)
                                .orElseThrow(() -> new ResourceNotFoundException("Loan not found: " + loanId));

                // Validate loan status - must be DISBURSED or ACTIVE
                if (!LoanStatus.DISBURSED.name().equals(loan.getStatus()) &&
                                !"ACTIVE".equals(loan.getStatus())) {
                        throw new ValidationException("Can only recalculate DISBURSED or ACTIVE loans. Current status: "
                                        + loan.getStatus());
                }

                // Get all repayments for this loan
                List<LoanRepayment> allRepayments = loanRepaymentRepository.findByLoanOrderByDueDateAsc(loan);

                // Separate paid and pending installments
                List<LoanRepayment> paidRepayments = allRepayments.stream()
                                .filter(r -> "PAID".equals(r.getStatus()) || "PARTIAL".equals(r.getStatus()))
                                .collect(Collectors.toList());

                List<LoanRepayment> pendingRepayments = allRepayments.stream()
                                .filter(r -> "PENDING".equals(r.getStatus()) || "OVERDUE".equals(r.getStatus()))
                                .collect(Collectors.toList());

                if (pendingRepayments.isEmpty()) {
                        throw new ValidationException(
                                        "No pending installments to recalculate. Loan may be fully paid.");
                }

                // Calculate remaining principal (original principal - sum of paid principal)
                BigDecimal paidPrincipal = paidRepayments.stream()
                                .map(LoanRepayment::getPrincipalDue)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal remainingPrincipal = loan.getPrincipal().subtract(paidPrincipal);

                // Calculate old total interest (from pending installments)
                BigDecimal oldTotalInterest = pendingRepayments.stream()
                                .map(LoanRepayment::getInterestDue)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                // Store old interest rate
                BigDecimal oldInterestRate = loan.getApprovedInterestRate();

                // Update loan interest rate
                loan.setApprovedInterestRate(newInterestRate);
                loan.setVersion(loan.getVersion() + 1);
                loanRepository.save(loan);

                // Delete pending installments
                loanRepaymentRepository.deleteAll(pendingRepayments);
                log.info("Deleted {} pending installments", pendingRepayments.size());

                // Generate new schedule for remaining tenure
                int remainingTenure = pendingRepayments.size();
                List<LoanRepayment> newRepayments;

                if (InterestType.FLAT.name().equals(loan.getInterestType())) {
                        newRepayments = generateFlatInterestScheduleForRecalculation(
                                        loan, remainingPrincipal, newInterestRate, remainingTenure,
                                        paidRepayments.size());
                } else if (InterestType.REDUCING_BALANCE.name().equals(loan.getInterestType())) {
                        newRepayments = generateReducingBalanceScheduleForRecalculation(
                                        loan, remainingPrincipal, newInterestRate, remainingTenure,
                                        paidRepayments.size());
                } else {
                        // Default to FLAT
                        newRepayments = generateFlatInterestScheduleForRecalculation(
                                        loan, remainingPrincipal, newInterestRate, remainingTenure,
                                        paidRepayments.size());
                }

                List<LoanRepayment> savedNewRepayments = loanRepaymentRepository.saveAll(newRepayments);

                // Calculate new total interest
                BigDecimal newTotalInterest = savedNewRepayments.stream()
                                .map(LoanRepayment::getInterestDue)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                // Update loan outstanding amount
                BigDecimal newTotalRepayable = savedNewRepayments.stream()
                                .map(LoanRepayment::getTotalDue)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                loan.setOutstandingAmount(newTotalRepayable);
                loanRepository.save(loan);

                // Create interest rate audit entry
                InterestRateAudit audit = new InterestRateAudit();
                audit.setId(UUID.randomUUID());
                audit.setLoan(loan);
                audit.setOldInterestRate(oldInterestRate);
                audit.setNewInterestRate(newInterestRate);
                audit.setChangedBy(admin);
                audit.setChangeReason(reason + " (Mid-loan recalculation)");
                audit.setCreatedAt(Instant.now());
                interestRateAuditRepository.save(audit);

                log.info("Loan recalculated successfully: loanId={}, oldRate={}%, newRate={}%, " +
                                "oldInterest={}, newInterest={}, savings={}",
                                loanId, oldInterestRate, newInterestRate, oldTotalInterest, newTotalInterest,
                                oldTotalInterest.subtract(newTotalInterest));

                // Build response
                return LoanRecalculationResponse.builder()
                                .loan(mapToLoanResponse(loan))
                                .oldInterestRate(oldInterestRate)
                                .newInterestRate(newInterestRate)
                                .paidInstallments(paidRepayments.size())
                                .remainingInstallments(savedNewRepayments.size())
                                .remainingPrincipal(remainingPrincipal)
                                .oldTotalInterest(oldTotalInterest)
                                .newTotalInterest(newTotalInterest)
                                .interestSavings(oldTotalInterest.subtract(newTotalInterest))
                                .newRepaymentSchedule(savedNewRepayments.stream()
                                                .map(this::mapToLoanRepaymentResponse)
                                                .collect(Collectors.toList()))
                                .build();
        }

        /**
         * Generate FLAT interest schedule for recalculation (starts from specific
         * month)
         */
        private List<LoanRepayment> generateFlatInterestScheduleForRecalculation(
                        Loan loan, BigDecimal remainingPrincipal, BigDecimal annualRate,
                        int remainingTenure, int monthsAlreadyPaid) {

                // Calculate total interest on remaining principal
                BigDecimal totalInterest = remainingPrincipal
                                .multiply(annualRate.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP))
                                .multiply(BigDecimal.valueOf(remainingTenure).divide(BigDecimal.valueOf(12), 10,
                                                RoundingMode.HALF_UP));

                BigDecimal totalRepayable = remainingPrincipal.add(totalInterest);
                BigDecimal emiAmount = totalRepayable.divide(BigDecimal.valueOf(remainingTenure), 2,
                                RoundingMode.HALF_UP);

                BigDecimal principalPerMonth = remainingPrincipal.divide(BigDecimal.valueOf(remainingTenure), 2,
                                RoundingMode.HALF_UP);
                BigDecimal interestPerMonth = totalInterest.divide(BigDecimal.valueOf(remainingTenure), 2,
                                RoundingMode.HALF_UP);

                List<LoanRepayment> repayments = new ArrayList<>();
                LocalDate currentDueDate = LocalDate.now().plusMonths(1);

                for (int i = 0; i < remainingTenure; i++) {
                        loanRepaymentResponse(loan, emiAmount, repayments, currentDueDate, interestPerMonth,
                                        principalPerMonth);
                        currentDueDate = currentDueDate.plusMonths(1);
                }

                return repayments;
        }

        /**
         * Generate REDUCING_BALANCE schedule for recalculation (starts from specific
         * month)
         */
        private List<LoanRepayment> generateReducingBalanceScheduleForRecalculation(
                        Loan loan, BigDecimal remainingPrincipal, BigDecimal annualRate,
                        int remainingTenure, int monthsAlreadyPaid) {

                BigDecimal monthlyRate = annualRate
                                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
                                .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);

                BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
                BigDecimal onePlusRPowerN = onePlusR.pow(remainingTenure);

                BigDecimal emi = remainingPrincipal
                                .multiply(monthlyRate)
                                .multiply(onePlusRPowerN)
                                .divide(onePlusRPowerN.subtract(BigDecimal.ONE), 2, RoundingMode.HALF_UP);

                List<LoanRepayment> repayments = new ArrayList<>();
                BigDecimal outstandingPrincipal = remainingPrincipal;
                LocalDate currentDueDate = LocalDate.now().plusMonths(1);

                for (int i = 0; i < remainingTenure; i++) {
                        BigDecimal interestDue = outstandingPrincipal
                                        .multiply(monthlyRate)
                                        .setScale(2, RoundingMode.HALF_UP);

                        BigDecimal principalDue = emi.subtract(interestDue);

                        // Last installment adjustment
                        if (i == remainingTenure - 1) {
                                principalDue = outstandingPrincipal;
                                emi = principalDue.add(interestDue);
                        }

                        loanRepaymentResponse(loan, emi, repayments, currentDueDate, interestDue, principalDue);
                        outstandingPrincipal = outstandingPrincipal.subtract(principalDue);
                        currentDueDate = currentDueDate.plusMonths(1);
                }

                return repayments;
        }

        // ==================== QUERY METHODS ====================

        @Transactional(readOnly = true)
        public LoanDetailsResponse getLoanDetails(UUID loanId, User user) {
                Loan loan = loanRepository.findById(loanId)
                                .orElseThrow(() -> new ResourceNotFoundException("Loan not found: " + loanId));

                // Validate user has access to this loan
                if (!loan.getUser().getId().equals(user.getId())) {
                        throw new ValidationException("You do not have access to this loan");
                }

                List<LoanRepayment> repayments = loanRepaymentRepository.findByLoanOrderByDueDateAsc(loan);

                BigDecimal totalRepayable = repayments.stream()
                                .map(LoanRepayment::getTotalDue)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal totalInterest = repayments.stream()
                                .map(LoanRepayment::getInterestDue)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                long remainingInstallments = repayments.stream()
                                .filter(r -> "PENDING".equals(r.getStatus()))
                                .count();

                return LoanDetailsResponse.builder()
                                .loan(mapToLoanResponse(loan))
                                .repaymentSchedule(repayments.stream()
                                                .map(this::mapToLoanRepaymentResponse)
                                                .collect(Collectors.toList()))
                                .totalRepayable(totalRepayable)
                                .totalInterest(totalInterest)
                                .remainingInstallments((int) remainingInstallments)
                                .build();
        }

        @Transactional(readOnly = true)
        public List<LoanResponse> getUserLoans(User user) {
                List<Loan> loans = loanRepository.findByUserOrderByCreatedAtDesc(user);
                return loans.stream()
                                .map(this::mapToLoanResponse)
                                .collect(Collectors.toList());
        }

        @Transactional(readOnly = true)
        public Page<LoanResponse> getLoansByStatus(String status, Pageable pageable) {
                return loanRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
                                .map(this::mapToLoanResponse);
        }

        // ==================== HELPER METHODS ====================

        private void validateUserEligibility(User user) {
                if (!user.getEnabled()) {
                        throw new ValidationException("User account is disabled");
                }
                if (!user.getVerified() || !"APPROVED".equals(user.getVerificationStatus())) {
                        throw new ValidationException("User account is not verified");
                }

                // Check for existing active loans (PENDING, APPROVED, ACTIVE, OVERDUE)
                long activeLoans = loanRepository.countByUserAndStatusIn(user,
                                List.of("PENDING", "APPROVED", "ACTIVE", "OVERDUE"));

                if (activeLoans > 0) {
                        throw new ValidationException(
                                        "You already have an active loan. Please repay it before applying for a new one.");
                }
        }

        private User assignAccountant() {
                // Simple implementation: find first enabled and verified user
                // In production, implement round-robin or load-based assignment with role
                // checking
                List<User> users = userRepository.findAll();
                return users.stream()
                                .filter(u -> u.getEnabled() && u.getVerified())
                                .findFirst()
                                .orElseThrow(() -> new ValidationException("No accountant available"));
        }

        // ==================== MAPPER METHODS ====================

        private void loanRepaymentResponse(Loan loan, BigDecimal emi, List<LoanRepayment> repayments,
                        LocalDate currentDueDate, BigDecimal interestDue, BigDecimal principalDue) {
                LoanRepayment repayment = new LoanRepayment();
                repayment.setId(UUID.randomUUID());
                repayment.setLoan(loan);
                repayment.setDueDate(currentDueDate);
                repayment.setPrincipalDue(principalDue);
                repayment.setInterestDue(interestDue);
                repayment.setFeeDue(BigDecimal.ZERO);
                repayment.setTotalDue(emi);
                repayment.setPaidAmount(BigDecimal.ZERO);
                repayment.setStatus("PENDING");
                repayment.setCreatedAt(Instant.now());

                repayments.add(repayment);
        }

        private LoanResponse mapToLoanResponse(Loan loan) {
                return LoanResponse.builder()
                                .id(loan.getId())
                                .userId(loan.getUser().getId())
                                .username(loan.getUser().getUsername())
                                .userFullName(loan.getUser().getFullName())
                                .principal(loan.getPrincipal())
                                .approvedInterestRate(loan.getApprovedInterestRate())
                                .interestType(loan.getInterestType())
                                .tenureMonths(loan.getTenureMonths())
                                .status(loan.getStatus())
                                .outstandingAmount(loan.getOutstandingAmount())
                                .currency(loan.getCurrency())
                                .approvedByUserId(loan.getApprovedBy() != null ? loan.getApprovedBy().getId() : null)
                                .approvedByUsername(loan.getApprovedBy() != null ? loan.getApprovedBy().getUsername()
                                                : null)
                                .approvedAt(loan.getApprovedAt())
                                .disbursedAt(loan.getDisbursedAt())
                                .createdAt(loan.getCreatedAt())
                                .version(loan.getVersion())
                                .build();
        }

        private LoanRepaymentResponse mapToLoanRepaymentResponse(LoanRepayment repayment) {
                return LoanRepaymentResponse.builder()
                                .id(repayment.getId())
                                .loanId(repayment.getLoan().getId())
                                .dueDate(repayment.getDueDate())
                                .principalDue(repayment.getPrincipalDue())
                                .interestDue(repayment.getInterestDue())
                                .feeDue(repayment.getFeeDue())
                                .totalDue(repayment.getTotalDue())
                                .paidAmount(repayment.getPaidAmount())
                                .status(repayment.getStatus())
                                .paidAt(repayment.getPaidAt())
                                .createdAt(repayment.getCreatedAt())
                                .build();
        }

        /**
         * Get all loans that have overdue installments
         * For Admin/Accountant use
         */
        public List<LoanResponse> getOverdueLoans() {
                return loanRepository.findLoansWithOverdueInstallments().stream()
                                .map(this::mapToLoanResponse)
                                .toList();
        }
}
