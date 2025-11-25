package tutul.ex.QarzMukti.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tutul.ex.QarzMukti.entities.LoanApproval;

import java.util.UUID;

public interface LoanApprovalRepository extends JpaRepository<LoanApproval, UUID> {}