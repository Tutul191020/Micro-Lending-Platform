package tutul.ex.QarzMukti.shared.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tutul.ex.QarzMukti.shared.entities.ReconciliationReport;
import tutul.ex.QarzMukti.shared.entities.ReconciliationResult;

import java.util.UUID;


import java.util.List;

public interface ReconciliationResultRepository extends JpaRepository<ReconciliationResult, UUID> {
    
    List<ReconciliationResult> findByReport(ReconciliationReport report);
    
    List<ReconciliationResult> findByStatus(String status);
}