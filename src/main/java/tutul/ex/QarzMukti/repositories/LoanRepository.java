package tutul.ex.QarzMukti.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import tutul.ex.QarzMukti.entities.Loan;
import tutul.ex.QarzMukti.entities.User;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoanRepository extends JpaRepository<Loan, UUID> {
    
    /**
     * Find all loans for a specific user
     */
    List<Loan> findByUserOrderByCreatedAtDesc(User user);
    
    /**
     * Find loans by status (paginated)
     */
    Page<Loan> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);
    
    /**
     * Find loans by user and status
     */
    List<Loan> findByUserAndStatus(User user, String status);
    
    /**
     * Find loan with pessimistic lock for update operations
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM Loan l WHERE l.id = :id")
    Optional<Loan> findByIdForUpdate(UUID id);
    
    /**
     * Count loans by status
     */
    long countByStatus(String status);
}