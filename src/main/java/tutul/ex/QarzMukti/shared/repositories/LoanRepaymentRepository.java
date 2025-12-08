package tutul.ex.QarzMukti.shared.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tutul.ex.QarzMukti.shared.entities.Loan;
import tutul.ex.QarzMukti.shared.entities.LoanRepayment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoanRepaymentRepository extends JpaRepository<LoanRepayment, UUID> {

    /**
     * Find all repayments for a specific loan ordered by due date
     */
    List<LoanRepayment> findByLoanOrderByDueDateAsc(Loan loan);

    /**
     * Find repayments by loan and status
     */
    List<LoanRepayment> findByLoanAndStatus(Loan loan, String status);

    /**
     * Count pending repayments for a loan
     */
    long countByLoanAndStatus(Loan loan, String status);

    /**
     * Find repayments by status and due date before a certain date
     */
    List<LoanRepayment> findByStatusAndDueDateBefore(String status, java.time.LocalDate date);

    /**
     * Find next unpaid installment after a specific date for a loan
     */
    @Query("SELECT r FROM LoanRepayment r WHERE r.loan = :loan " +
            "AND r.dueDate > :afterDate " +
            "AND r.status IN ('PENDING', 'PARTIALLY_PAID') " +
            "ORDER BY r.dueDate ASC")
    Optional<LoanRepayment> findNextUnpaidInstallment(
            @Param("loan") Loan loan,
            @Param("afterDate") LocalDate afterDate);

    /**
     * Find all future unpaid installments after a specific date
     */
    @Query("SELECT r FROM LoanRepayment r WHERE r.loan = :loan " +
            "AND r.status IN ('PENDING', 'PARTIALLY_PAID') " +
            "AND r.dueDate > :afterDate " +
            "ORDER BY r.dueDate ASC")
    List<LoanRepayment> findFutureUnpaidInstallments(
            @Param("loan") Loan loan,
            @Param("afterDate") LocalDate afterDate);

    /**
     * Calculate total remaining debt for a loan
     */
    @Query("SELECT COALESCE(SUM(r.totalDue - r.paidAmount), 0) " +
            "FROM LoanRepayment r WHERE r.loan = :loan " +
            "AND r.status IN ('PENDING', 'PARTIALLY_PAID')")
    BigDecimal calculateTotalRemainingDebt(@Param("loan") Loan loan);
}