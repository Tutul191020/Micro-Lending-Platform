package tutul.ex.QarzMukti.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tutul.ex.QarzMukti.entities.ReconciliationResult;

import java.util.UUID;


public interface ReconciliationResultRepository extends JpaRepository<ReconciliationResult, UUID> {}