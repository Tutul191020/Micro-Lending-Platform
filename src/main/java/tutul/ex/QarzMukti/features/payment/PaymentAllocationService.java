package tutul.ex.QarzMukti.features.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tutul.ex.QarzMukti.shared.dto.AllocationDetail;
import tutul.ex.QarzMukti.shared.dto.PaymentAllocationResult;
import tutul.ex.QarzMukti.shared.entities.Loan;
import tutul.ex.QarzMukti.shared.entities.LoanRepayment;
import tutul.ex.QarzMukti.shared.entities.PaymentAllocation;
import tutul.ex.QarzMukti.shared.entities.PaymentTransaction;
import tutul.ex.QarzMukti.shared.repositories.LoanRepaymentRepository;
import tutul.ex.QarzMukti.shared.repositories.PaymentAllocationRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentAllocationService {

    private final PaymentAllocationRepository paymentAllocationRepository;
    private final LoanRepaymentRepository loanRepaymentRepository;

    /**
     * Main method to allocate payment across installments
     */
    @Transactional
    public PaymentAllocationResult allocatePayment(
            PaymentTransaction transaction,
            BigDecimal amount,
            LoanRepayment targetRepayment) {

        log.info("Allocating payment: txId={}, amount={}, repaymentId={}",
                transaction.getId(), amount, targetRepayment.getId());

        BigDecimal remainingAmount = targetRepayment.getRemainingAmount();
        int comparison = amount.compareTo(remainingAmount);

        if (comparison == 0) {
            // Exact payment
            return handleExactPayment(transaction, targetRepayment);
        } else if (comparison < 0) {
            // Underpayment
            return handleUnderpayment(transaction, amount, targetRepayment);
        } else {
            // Overpayment
            return handleOverpayment(transaction, amount, targetRepayment);
        }
    }

    /**
     * Handle exact payment (amount == remaining amount)
     */
    private PaymentAllocationResult handleExactPayment(
            PaymentTransaction transaction,
            LoanRepayment repayment) {

        log.info("Handling exact payment for repayment {}", repayment.getId());

        BigDecimal amount = repayment.getRemainingAmount();

        // Update repayment
        repayment.setPaidAmount(repayment.getPaidAmount().add(amount));
        repayment.setStatus("PAID");
        repayment.setPaidAt(Instant.now());

        // Create allocation record
        PaymentAllocation allocation = PaymentAllocation.builder()
                .id(UUID.randomUUID())
                .transaction(transaction)
                .repayment(repayment)
                .allocatedAmount(amount)
                .allocationType("CURRENT_INSTALLMENT")
                .createdAt(Instant.now())
                .build();

        paymentAllocationRepository.save(allocation);

        AllocationDetail detail = AllocationDetail.builder()
                .repaymentId(repayment.getId())
                .dueDate(repayment.getDueDate())
                .allocatedAmount(amount)
                .allocationType("CURRENT_INSTALLMENT")
                .newRemainingAmount(BigDecimal.ZERO)
                .build();

        return PaymentAllocationResult.builder()
                .transactionId(transaction.getId())
                .totalAmount(amount)
                .primaryRepayment(repayment)
                .primaryAllocation(amount)
                .allocations(List.of(detail))
                .allocationStrategy("EXACT")
                .affectedRepayments(List.of(repayment))
                .build();
    }

    /**
     * Handle underpayment (amount < remaining amount)
     */
    private PaymentAllocationResult handleUnderpayment(
            PaymentTransaction transaction,
            BigDecimal amount,
            LoanRepayment repayment) {

        log.info("Handling underpayment: repayment={}, amount={}, remaining={}",
                repayment.getId(), amount, repayment.getRemainingAmount());

        List<AllocationDetail> allocationDetails = new ArrayList<>();
        List<LoanRepayment> affectedRepayments = new ArrayList<>();

        // Update current repayment
        repayment.setPaidAmount(repayment.getPaidAmount().add(amount));
        repayment.setStatus("PARTIALLY_PAID");

        // Create allocation for current installment
        PaymentAllocation currentAllocation = PaymentAllocation.builder()
                .id(UUID.randomUUID())
                .transaction(transaction)
                .repayment(repayment)
                .allocatedAmount(amount)
                .allocationType("CURRENT_INSTALLMENT")
                .createdAt(Instant.now())
                .build();

        paymentAllocationRepository.save(currentAllocation);
        affectedRepayments.add(repayment);

        allocationDetails.add(AllocationDetail.builder()
                .repaymentId(repayment.getId())
                .dueDate(repayment.getDueDate())
                .allocatedAmount(amount)
                .allocationType("CURRENT_INSTALLMENT")
                .newRemainingAmount(repayment.getRemainingAmount())
                .build());

        // Calculate deficit and transfer to next installment
        BigDecimal deficit = repayment.getRemainingAmount();

        if (deficit.compareTo(BigDecimal.ZERO) > 0) {
            LoanRepayment nextInstallment = loanRepaymentRepository
                    .findNextUnpaidInstallment(repayment.getLoan(), repayment.getDueDate())
                    .orElse(null);

            if (nextInstallment != null) {
                log.info("Transferring deficit of {} to next installment {}",
                        deficit, nextInstallment.getId());

                // Update next installment
                nextInstallment.setTotalDue(nextInstallment.getTotalDue().add(deficit));
                nextInstallment.setHasDeficitFromPreviousMonth(true);
                affectedRepayments.add(nextInstallment);

                // Create deficit transfer allocation
                PaymentAllocation deficitAllocation = PaymentAllocation.builder()
                        .id(UUID.randomUUID())
                        .transaction(transaction)
                        .repayment(nextInstallment)
                        .allocatedAmount(deficit)
                        .allocationType("DEFICIT_TRANSFER")
                        .createdAt(Instant.now())
                        .build();

                paymentAllocationRepository.save(deficitAllocation);

                allocationDetails.add(AllocationDetail.builder()
                        .repaymentId(nextInstallment.getId())
                        .dueDate(nextInstallment.getDueDate())
                        .allocatedAmount(deficit)
                        .allocationType("DEFICIT_TRANSFER")
                        .newRemainingAmount(nextInstallment.getRemainingAmount())
                        .build());
            }
        }

        return PaymentAllocationResult.builder()
                .transactionId(transaction.getId())
                .totalAmount(amount)
                .primaryRepayment(repayment)
                .primaryAllocation(amount)
                .allocations(allocationDetails)
                .allocationStrategy("PARTIAL")
                .affectedRepayments(affectedRepayments)
                .build();
    }

    /**
     * Handle overpayment (amount > remaining amount)
     */
    private PaymentAllocationResult handleOverpayment(
            PaymentTransaction transaction,
            BigDecimal amount,
            LoanRepayment repayment) {

        log.info("Handling overpayment: repayment={}, amount={}, remaining={}",
                repayment.getId(), amount, repayment.getRemainingAmount());

        List<AllocationDetail> allocationDetails = new ArrayList<>();
        List<LoanRepayment> affectedRepayments = new ArrayList<>();

        // Pay off current installment fully
        BigDecimal currentPayment = repayment.getRemainingAmount();
        repayment.setPaidAmount(repayment.getPaidAmount().add(currentPayment));
        repayment.setStatus("PAID");
        repayment.setPaidAt(Instant.now());

        // Create allocation for current installment
        PaymentAllocation currentAllocation = PaymentAllocation.builder()
                .id(UUID.randomUUID())
                .transaction(transaction)
                .repayment(repayment)
                .allocatedAmount(currentPayment)
                .allocationType("CURRENT_INSTALLMENT")
                .createdAt(Instant.now())
                .build();

        paymentAllocationRepository.save(currentAllocation);
        affectedRepayments.add(repayment);

        allocationDetails.add(AllocationDetail.builder()
                .repaymentId(repayment.getId())
                .dueDate(repayment.getDueDate())
                .allocatedAmount(currentPayment)
                .allocationType("CURRENT_INSTALLMENT")
                .newRemainingAmount(BigDecimal.ZERO)
                .build());

        // Calculate excess and distribute to future installments
        BigDecimal excess = amount.subtract(currentPayment);

        if (excess.compareTo(BigDecimal.ZERO) > 0) {
            List<AllocationDetail> excessAllocations = distributeExcessToFutureInstallments(
                    excess, repayment.getLoan(), transaction, repayment.getDueDate());

            allocationDetails.addAll(excessAllocations);

            // Add affected repayments from excess distribution
            excessAllocations.forEach(detail -> {
                loanRepaymentRepository.findById(detail.getRepaymentId())
                        .ifPresent(affectedRepayments::add);
            });
        }

        return PaymentAllocationResult.builder()
                .transactionId(transaction.getId())
                .totalAmount(amount)
                .primaryRepayment(repayment)
                .primaryAllocation(currentPayment)
                .allocations(allocationDetails)
                .allocationStrategy("OVERPAYMENT")
                .affectedRepayments(affectedRepayments)
                .build();
    }

    /**
     * Distribute excess payment proportionally across future installments
     */
    private List<AllocationDetail> distributeExcessToFutureInstallments(
            BigDecimal excess,
            Loan loan,
            PaymentTransaction transaction,
            java.time.LocalDate afterDate) {

        log.info("Distributing excess of {} to future installments", excess);

        List<AllocationDetail> allocations = new ArrayList<>();
        List<LoanRepayment> futureInstallments = loanRepaymentRepository
                .findFutureUnpaidInstallments(loan, afterDate);

        if (futureInstallments.isEmpty()) {
            log.warn("No future installments found to distribute excess");
            return allocations;
        }

        // Calculate total future debt
        BigDecimal totalFutureDebt = futureInstallments.stream()
                .map(LoanRepayment::getRemainingAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalFutureDebt.compareTo(BigDecimal.ZERO) == 0) {
            log.warn("Total future debt is zero, cannot distribute proportionally");
            return allocations;
        }

        // Distribute proportionally
        BigDecimal remainingExcess = excess;

        for (int i = 0; i < futureInstallments.size(); i++) {
            LoanRepayment future = futureInstallments.get(i);
            BigDecimal allocation;

            if (i == futureInstallments.size() - 1) {
                // Last installment gets remaining excess to handle rounding
                allocation = remainingExcess;
            } else {
                // Proportional allocation
                BigDecimal proportion = future.getRemainingAmount()
                        .divide(totalFutureDebt, 4, RoundingMode.HALF_UP);
                allocation = excess.multiply(proportion)
                        .setScale(2, RoundingMode.HALF_UP);
            }

            // Ensure we don't allocate more than the installment's remaining amount
            allocation = allocation.min(future.getRemainingAmount());

            if (allocation.compareTo(BigDecimal.ZERO) > 0) {
                // Update installment
                future.setTotalDue(future.getTotalDue().subtract(allocation));

                // Create allocation record
                PaymentAllocation advanceAllocation = PaymentAllocation.builder()
                        .id(UUID.randomUUID())
                        .transaction(transaction)
                        .repayment(future)
                        .allocatedAmount(allocation)
                        .allocationType("ADVANCE_PAYMENT")
                        .createdAt(Instant.now())
                        .build();

                paymentAllocationRepository.save(advanceAllocation);

                allocations.add(AllocationDetail.builder()
                        .repaymentId(future.getId())
                        .dueDate(future.getDueDate())
                        .allocatedAmount(allocation)
                        .allocationType("ADVANCE_PAYMENT")
                        .newRemainingAmount(future.getRemainingAmount())
                        .build());

                remainingExcess = remainingExcess.subtract(allocation);
            }
        }

        log.info("Distributed excess to {} future installments", allocations.size());
        return allocations;
    }
}
