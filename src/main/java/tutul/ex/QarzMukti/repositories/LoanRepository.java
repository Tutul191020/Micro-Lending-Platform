package tutul.ex.QarzMukti.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tutul.ex.QarzMukti.entities.Loan;

import java.util.UUID;

public interface LoanRepository extends JpaRepository<Loan, UUID> {}