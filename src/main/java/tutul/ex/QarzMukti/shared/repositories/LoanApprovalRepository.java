package tutul.ex.QarzMukti.shared.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tutul.ex.QarzMukti.shared.entities.Loan;
import tutul.ex.QarzMukti.shared.entities.LoanApproval;
import tutul.ex.QarzMukti.shared.entities.User;

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