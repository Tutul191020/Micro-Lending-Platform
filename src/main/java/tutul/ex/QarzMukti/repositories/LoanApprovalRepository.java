package tutul.ex.QarzMukti.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tutul.ex.QarzMukti.entities.Loan;
import tutul.ex.QarzMukti.entities.LoanApproval;
import tutul.ex.QarzMukti.entities.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoanApprovalRepository extends JpaRepository<LoanApproval, UUID> {
    
    /**
     * Find approval record by loan
     */
    Optional<LoanApproval> findByLoan(Loan loan);
    
    /**
     * Find all approvals assigned to an accountant
     */
    List<LoanApproval> findByAssignedAccountantOrderByCreatedAtDesc(User accountant);
    
    /**
     * Find approvals by status
     */
    List<LoanApproval> findByStatusOrderByCreatedAtDesc(String status);
}