package tutul.ex.QarzMukti.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tutul.ex.QarzMukti.common.ResourceNotFoundException;
import tutul.ex.QarzMukti.common.ValidationException;
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
    private final VirtualFundPoolRepository virtualFundPoolRepository;
    private final UserRepository userRepository;

    /**
     * Initiate a payment for a loan repayment installment
     */
    @Transactional
    public PaymentInitiationResponse initiateRepayment(User user, PaymentInitiationRequest request) {
        log.info("Initiating repayment: user={}, repaymentId={}", user.getId(), request.getRepaymentId());

        LoanRepayment repayment = loanRepaymentRepository.findById(request.getRepaymentId())
                .orElseThrow(() -> new ResourceNotFoundException("Repayment not found"));

        if (!repayment.getLoan().getUser().getId().equals(user.getId())) {
            throw new ValidationException("You can only pay for your own loans");
        }

        if ("PAID".equals(repayment.getStatus())) {
            throw new ValidationException("This installment is already paid");
        }

        // Create pending payment transaction
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setId(UUID.randomUUID());
        transaction.setRepayment(repayment);
        transaction.setLoan(repayment.getLoan());
        transaction.setUser(user);
        transaction.setGateway(request.getGateway());
        transaction.setAmount(repayment.getTotalDue()); // Full payment for now
        transaction.setStatus("PENDING");
        transaction.setInitiatedAt(Instant.now());
        transaction.setIdempotencyKey(UUID.randomUUID()); // Simple idempotency for now

        paymentTransactionRepository.save(transaction);

        // Call mock gateway
        return mockPaymentGatewayService.initiatePayment(
                transaction.getId(), 
                transaction.getAmount(), 
                request.getGateway()
        );
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

        // 1. Update Repayment Status
        repayment.setStatus("PAID");
        repayment.setPaidAmount(transaction.getAmount());
        repayment.setPaidAt(Instant.now());
        loanRepaymentRepository.save(repayment);

        // 2. Update Loan Outstanding Amount
        loan.setOutstandingAmount(loan.getOutstandingAmount().subtract(transaction.getAmount()));
        
        // Check if loan is fully paid
        long pendingCount = loanRepaymentRepository.countByLoanAndStatus(loan, "PENDING");
        if (pendingCount == 0) {
            loan.setStatus("FULLY_PAID");
        }
        loanRepository.save(loan);

        // 3. Ledger Entry (Credit Accountant/Lender)
        // For now, we assume the money goes to a system pool or simply recorded without specific pools if not available
        // In a real scenario, we would credit the specific pool of the lender
        
        ledgerService.createLoanRepaymentEntry(
                null, // From User (no pool)
                null, // To System/Lender (no specific pool logic implemented yet)
                transaction.getAmount(),
                repayment.getId(),
                transaction.getUser()
        );

        // 4. Transaction Log
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
                java.util.Map.of("repaymentId", repayment.getId().toString()),
                transaction.getUser()
        );
        
        log.info("Payment processed successfully: txId={}", transaction.getId());
    }

    private void handleFailedPayment(PaymentTransaction transaction) {
        transaction.setStatus("FAILED");
        paymentTransactionRepository.save(transaction);
        log.info("Payment marked as failed: txId={}", transaction.getId());
    }
}
