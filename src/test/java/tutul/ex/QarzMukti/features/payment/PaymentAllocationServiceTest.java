package tutul.ex.QarzMukti.features.payment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tutul.ex.QarzMukti.shared.dto.PaymentAllocationResult;
import tutul.ex.QarzMukti.shared.entities.Loan;
import tutul.ex.QarzMukti.shared.entities.LoanRepayment;
import tutul.ex.QarzMukti.shared.entities.PaymentTransaction;
import tutul.ex.QarzMukti.shared.repositories.LoanRepaymentRepository;
import tutul.ex.QarzMukti.shared.repositories.PaymentAllocationRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the payment allocation engine: exact payments, underpayments
 * with deficit carry-forward, and overpayments with proportional advance
 * distribution across future installments.
 */
@ExtendWith(MockitoExtension.class)
class PaymentAllocationServiceTest {

    @Mock
    private PaymentAllocationRepository paymentAllocationRepository;

    @Mock
    private LoanRepaymentRepository loanRepaymentRepository;

    @InjectMocks
    private PaymentAllocationService allocationService;

    private Loan loan;
    private PaymentTransaction transaction;

    @BeforeEach
    void setUp() {
        loan = new Loan();
        loan.setId(UUID.randomUUID());

        transaction = new PaymentTransaction();
        transaction.setId(UUID.randomUUID());
        transaction.setLoan(loan);
    }

    private LoanRepayment installment(String totalDue, String paidAmount, LocalDate dueDate) {
        LoanRepayment repayment = new LoanRepayment();
        repayment.setId(UUID.randomUUID());
        repayment.setLoan(loan);
        repayment.setDueDate(dueDate);
        repayment.setPrincipalDue(new BigDecimal(totalDue));
        repayment.setInterestDue(BigDecimal.ZERO);
        repayment.setTotalDue(new BigDecimal(totalDue));
        repayment.setPaidAmount(new BigDecimal(paidAmount));
        repayment.setStatus("PENDING");
        repayment.setCreatedAt(Instant.now());
        return repayment;
    }

    @Test
    @DisplayName("Exact payment settles the installment and records a single allocation")
    void exactPaymentSettlesInstallment() {
        LoanRepayment target = installment("1000.00", "0.00", LocalDate.now().plusDays(5));

        PaymentAllocationResult result = allocationService.allocatePayment(
                transaction, new BigDecimal("1000.00"), target);

        assertThat(target.getStatus()).isEqualTo("PAID");
        assertThat(target.getPaidAmount()).isEqualByComparingTo("1000.00");
        assertThat(target.getRemainingAmount()).isEqualByComparingTo("0.00");
        assertThat(target.getPaidAt()).isNotNull();
        assertThat(result.getAllocationStrategy()).isEqualTo("EXACT");
        assertThat(result.getAllocations()).hasSize(1);
        assertThat(result.getAllocations().get(0).getAllocationType()).isEqualTo("CURRENT_INSTALLMENT");
        verify(paymentAllocationRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Underpayment marks installment PARTIALLY_PAID and carries the deficit to the next one")
    void underpaymentTransfersDeficitToNextInstallment() {
        LoanRepayment target = installment("1000.00", "0.00", LocalDate.now().plusDays(5));
        LoanRepayment next = installment("1000.00", "0.00", LocalDate.now().plusMonths(1).plusDays(5));

        when(loanRepaymentRepository.findNextUnpaidInstallment(loan, target.getDueDate()))
                .thenReturn(Optional.of(next));

        PaymentAllocationResult result = allocationService.allocatePayment(
                transaction, new BigDecimal("600.00"), target);

        assertThat(target.getStatus()).isEqualTo("PARTIALLY_PAID");
        assertThat(target.getPaidAmount()).isEqualByComparingTo("600.00");

        // Deficit of 400 is added to the next installment and flagged
        assertThat(next.getTotalDue()).isEqualByComparingTo("1400.00");
        assertThat(next.getHasDeficitFromPreviousMonth()).isTrue();

        assertThat(result.getAllocationStrategy()).isEqualTo("PARTIAL");
        assertThat(result.getAllocations()).hasSize(2);
        assertThat(result.getAllocations())
                .extracting("allocationType")
                .containsExactly("CURRENT_INSTALLMENT", "DEFICIT_TRANSFER");
        assertThat(result.getAffectedRepayments()).containsExactly(target, next);
        verify(paymentAllocationRepository, times(2)).save(any());
    }

    @Test
    @DisplayName("Underpayment with no future installment records only the partial allocation")
    void underpaymentWithoutNextInstallmentSkipsDeficitTransfer() {
        LoanRepayment target = installment("1000.00", "0.00", LocalDate.now().plusDays(5));

        when(loanRepaymentRepository.findNextUnpaidInstallment(loan, target.getDueDate()))
                .thenReturn(Optional.empty());

        PaymentAllocationResult result = allocationService.allocatePayment(
                transaction, new BigDecimal("600.00"), target);

        assertThat(target.getStatus()).isEqualTo("PARTIALLY_PAID");
        assertThat(result.getAllocations()).hasSize(1);
        assertThat(result.getAffectedRepayments()).containsExactly(target);
        verify(paymentAllocationRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Overpayment settles current installment and distributes excess proportionally")
    void overpaymentDistributesExcessProportionally() {
        LoanRepayment target = installment("1000.00", "0.00", LocalDate.now().plusDays(5));
        // Future debt: 1000 + 2000 = 3000; excess of 600 splits 1/3 : 2/3
        LoanRepayment futureA = installment("1000.00", "0.00", LocalDate.now().plusMonths(1).plusDays(5));
        LoanRepayment futureB = installment("2000.00", "0.00", LocalDate.now().plusMonths(2).plusDays(5));

        when(loanRepaymentRepository.findFutureUnpaidInstallments(loan, target.getDueDate()))
                .thenReturn(List.of(futureA, futureB));
        when(loanRepaymentRepository.findById(futureA.getId())).thenReturn(Optional.of(futureA));
        when(loanRepaymentRepository.findById(futureB.getId())).thenReturn(Optional.of(futureB));

        PaymentAllocationResult result = allocationService.allocatePayment(
                transaction, new BigDecimal("1600.00"), target);

        assertThat(target.getStatus()).isEqualTo("PAID");
        assertThat(result.getAllocationStrategy()).isEqualTo("OVERPAYMENT");
        assertThat(result.getPrimaryAllocation()).isEqualByComparingTo("1000.00");

        // 600 * (1000/3000) = 199.98 to A; remainder 400.02 to B (rounding-safe last item)
        assertThat(futureA.getTotalDue()).isEqualByComparingTo("800.02");
        assertThat(futureB.getTotalDue()).isEqualByComparingTo("1599.98");

        assertThat(result.getAllocations()).hasSize(3);
        assertThat(result.getAllocations())
                .extracting("allocationType")
                .containsExactly("CURRENT_INSTALLMENT", "ADVANCE_PAYMENT", "ADVANCE_PAYMENT");
        assertThat(result.getAffectedRepayments()).containsExactly(target, futureA, futureB);
    }

    @Test
    @DisplayName("Overpayment with no future installments keeps the excess unallocated")
    void overpaymentWithoutFutureInstallmentsAllocatesOnlyCurrent() {
        LoanRepayment target = installment("1000.00", "0.00", LocalDate.now().plusDays(5));

        when(loanRepaymentRepository.findFutureUnpaidInstallments(loan, target.getDueDate()))
                .thenReturn(List.of());

        PaymentAllocationResult result = allocationService.allocatePayment(
                transaction, new BigDecimal("1600.00"), target);

        assertThat(target.getStatus()).isEqualTo("PAID");
        assertThat(result.getAllocations()).hasSize(1);
        verify(paymentAllocationRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Advance allocation never exceeds an installment's remaining amount")
    void advanceAllocationIsCappedAtRemainingAmount() {
        LoanRepayment target = installment("1000.00", "0.00", LocalDate.now().plusDays(5));
        LoanRepayment future = installment("100.00", "0.00", LocalDate.now().plusMonths(1).plusDays(5));

        when(loanRepaymentRepository.findFutureUnpaidInstallments(loan, target.getDueDate()))
                .thenReturn(List.of(future));
        when(loanRepaymentRepository.findById(future.getId())).thenReturn(Optional.of(future));

        allocationService.allocatePayment(transaction, new BigDecimal("1600.00"), target);

        // Excess is 600 but the future installment only owes 100
        assertThat(future.getTotalDue()).isEqualByComparingTo("0.00");
        verify(paymentAllocationRepository, times(2)).save(any());
        verify(loanRepaymentRepository, never()).findNextUnpaidInstallment(any(), any());
    }
}
