package tutul.ex.QarzMukti.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tutul.ex.QarzMukti.entities.Loan;
import tutul.ex.QarzMukti.entities.LoanRepayment;

import java.util.List;
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
}