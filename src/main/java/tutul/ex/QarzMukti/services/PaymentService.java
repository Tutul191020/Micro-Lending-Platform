package tutul.ex.QarzMukti.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tutul.ex.QarzMukti.common.ResourceNotFoundException;
import tutul.ex.QarzMukti.common.ValidationException;
import tutul.ex.QarzMukti.dto.PaymentAllocationResult;
import tutul.ex.QarzMukti.dto.PaymentInitiationRequest;
import tutul.ex.QarzMukti.dto.PaymentInitiationResponse;
import tutul.ex.QarzMukti.dto.PaymentWebhookRequest;
import tutul.ex.QarzMukti.entities.*;
import tutul.ex.QarzMukti.repositories.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final LoanRepaymentRepository loanRepaymentRepository;
    private final LoanRepository loanRepository;
    private final MockPaymentGatewayService mockPaymentGatewayService;
    private final LedgerService ledgerService;
    private final TransactionLogService transactionLogService;
    private final PaymentAllocationService paymentAllocationService;
    private final UserService userService;

    /**
     * Initiate a payment for a loan repayment installment
     */
    @Transactional
    public PaymentInitiationResponse initiateRepayment(User user, PaymentInitiationRequest request) {
        log.info("Initiating repayment: user={}, repaymentId={}, amount={}",
                user.getId(), request.getRepaymentId(), request.getAmount());

        LoanRepayment repayment = loanRepaymentRepository.findById(request.getRepaymentId())
                .orElseThrow(() -> new ResourceNotFoundException("Repayment not found"));

        if (!repayment.getLoan().getUser().getId().equals(user.getId())) {
            throw new ValidationException("You can only pay for your own loans");
        }

        if ("PAID".equals(repayment.getStatus())) {
            throw new ValidationException("This installment is already paid");
        }

        // Validate monthly deadline - if past the month, handle overdue
        if (!repayment.isWithinCurrentMonth()) {
            if ("PENDING".equals(repayment.getStatus()) || "PARTIALLY_PAID".equals(repayment.getStatus())) {
                handleOverdueInstallment(repayment);
                throw new ValidationException(
                        "This installment is overdue and has been transferred to the next month. " +
                                "Please pay the next month's installment instead.");
            }
        }

        // Determine payment amount
        BigDecimal paymentAmount = request.getAmount() != null
                ? request.getAmount()
                : repayment.getRemainingAmount();

        // Validate payment amount is positive
        if (paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Payment amount must be positive");
        }

        // Check for consecutive underpayment prevention
        BigDecimal minimumRequired = repayment.getMinimumPaymentRequired();
        if (minimumRequired.compareTo(BigDecimal.ZERO) > 0) {
            // This installment has deficit from previous month
            if (paymentAmount.compareTo(minimumRequired) < 0) {
                throw new ValidationException(
                        String.format(
                                "Cannot underpay for consecutive months. " +
                                        "This installment has a carried-over deficit from last month. " +
                                        "Minimum payment required: $%.2f (original installment amount). " +
                                        "You can pay between $%.2f and $%.2f.",
                                minimumRequired,
                                minimumRequired,
                                repayment.getRemainingAmount()));
            }
        }

        // Optional: Set general minimum payment requirement (e.g., 10% of remaining)
        // Uncomment if you want to enforce a minimum payment percentage
        // BigDecimal generalMinimum = calculateMinimumPayment(repayment);
        // if (paymentAmount.compareTo(generalMinimum) < 0) {
        // throw new ValidationException(
        // String.format("Minimum payment required: $%.2f", generalMinimum)
        // );
        // }

        // Create pending payment transaction
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setId(UUID.randomUUID());
        transaction.setRepayment(repayment);
        transaction.setLoan(repayment.getLoan());
        transaction.setUser(user);
        transaction.setGateway(request.getGateway());
        transaction.setAmount(paymentAmount);
        transaction.setStatus("PENDING");
        transaction.setInitiatedAt(Instant.now());
        transaction.setIdempotencyKey(UUID.randomUUID());

        paymentTransactionRepository.save(transaction);

        // Call mock gateway
        return mockPaymentGatewayService.initiatePayment(
                transaction.getId(),
                transaction.getAmount(),
                request.getGateway());
    }

    /**
     * Handle overdue installment by transferring deficit to next month
     */
    private void handleOverdueInstallment(LoanRepayment repayment) {
        log.warn("Installment {} is overdue. Due date: {}",
                repayment.getId(), repayment.getDueDate());

        repayment.setStatus("OVERDUE");

        // Transfer remaining balance to next month
        BigDecimal deficit = repayment.getRemainingAmount();
        if (deficit.compareTo(BigDecimal.ZERO) > 0) {
            loanRepaymentRepository
                    .findNextUnpaidInstallment(repayment.getLoan(), repayment.getDueDate())
                    .ifPresent(nextInstallment -> {
                        nextInstallment.setTotalDue(
                                nextInstallment.getTotalDue().add(deficit));
                        nextInstallment.setHasDeficitFromPreviousMonth(true);
                        loanRepaymentRepository.save(nextInstallment);

                        log.info("Transferred deficit of {} to next installment {}",
                                deficit, nextInstallment.getId());
                    });
        }

        loanRepaymentRepository.save(repayment);

        // Deduct credit score for overdue
        userService.updateCreditScore(repayment.getLoan().getUser(), -20);
    }

    /**
     * Process webhook callback from payment gateway
     */
    @Transactional
    public void processPaymentWebhook(PaymentWebhookRequest payload) {
        log.info("Processing payment webhook: paymentId={}, status={}", payload.getPaymentId(), payload.getStatus());

        PaymentTransaction transaction = paymentTransactionRepository.findById(payload.getPaymentId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment transaction not found"));

        if (!"PENDING".equals(transaction.getStatus())) {
            log.warn("Transaction {} is already processed (status={})", transaction.getId(), transaction.getStatus());
            return; // Idempotent: ignore if already processed
        }

        transaction.setGatewayTxId(payload.getGatewayTxId());
        transaction.setCompletedAt(Instant.now());

        if ("SUCCESS".equals(payload.getStatus())) {
            handleSuccessfulPayment(transaction);
        } else {
            handleFailedPayment(transaction);
        }
    }

    private void handleSuccessfulPayment(PaymentTransaction transaction) {
        transaction.setStatus("SUCCESS");
        paymentTransactionRepository.save(transaction);

        LoanRepayment repayment = transaction.getRepayment();
        Loan loan = transaction.getLoan();

        // 1. Use PaymentAllocationService to handle allocation
        PaymentAllocationResult allocationResult = paymentAllocationService.allocatePayment(
                transaction,
                transaction.getAmount(),
                repayment);

        // 2. Save all affected repayments
        loanRepaymentRepository.saveAll(allocationResult.getAffectedRepayments());

        log.info("Payment allocated: strategy={}, affectedRepayments={}",
                allocationResult.getAllocationStrategy(),
                allocationResult.getAffectedRepayments().size());

        // 3. Update Loan Outstanding Amount
        loan.setOutstandingAmount(loan.getOutstandingAmount().subtract(transaction.getAmount()));

        // Check if loan is fully paid
        long pendingCount = loanRepaymentRepository.countByLoanAndStatus(loan, "PENDING");
        long partialCount = loanRepaymentRepository.countByLoanAndStatus(loan, "PARTIALLY_PAID");
        if (pendingCount == 0 && partialCount == 0) {
            loan.setStatus("FULLY_PAID");
            // Bonus for loan completion
            userService.updateCreditScore(loan.getUser(), 50);
        }
        loanRepository.save(loan);

        // Add credit score for on-time payment
        userService.updateCreditScore(loan.getUser(), 10);

        // 4. Ledger Entry (Credit Accountant/Lender)
        ledgerService.createLoanRepaymentEntry(
                null, // From User (no pool)
                null, // To System/Lender (no specific pool logic implemented yet)
                transaction.getAmount(),
                repayment.getId(),
                transaction.getUser());

        // 5. Transaction Log
        transactionLogService.logTransaction(
                "LOAN_REPAYMENT",
                transaction.getAmount(),
                transaction.getUser(),
                null, // To System
                null,
                null,
                transaction.getId(),
                "PAYMENT",
                "Loan repayment received via " + transaction.getGateway(),
                java.util.Map.of(
                        "repaymentId", repayment.getId().toString(),
                        "allocationStrategy", allocationResult.getAllocationStrategy(),
                        "affectedRepayments", String.valueOf(allocationResult.getAffectedRepayments().size())),
                transaction.getUser());

        log.info("Payment processed successfully: txId={}", transaction.getId());
    }

    private void handleFailedPayment(PaymentTransaction transaction) {
        transaction.setStatus("FAILED");
        paymentTransactionRepository.save(transaction);
        log.info("Payment marked as failed: txId={}", transaction.getId());
    }
}
