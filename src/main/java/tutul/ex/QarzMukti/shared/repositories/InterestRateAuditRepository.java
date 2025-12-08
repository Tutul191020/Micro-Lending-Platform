package tutul.ex.QarzMukti.shared.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import tutul.ex.QarzMukti.shared.entities.InterestRateAudit;
import tutul.ex.QarzMukti.shared.entities.Loan;

import java.util.List;
import java.util.UUID;

public interface InterestRateAuditRepository extends JpaRepository<InterestRateAudit, UUID> {
    
    /**
     * Find all interest rate changes for a specific loan
     */
    List<InterestRateAudit> findByLoanOrderByCreatedAtDesc(Loan loan);
    
    /**
     * Get paginated audit history
     */
    Page<InterestRateAudit> findAllByOrderByCreatedAtDesc(Pageable pageable);
}