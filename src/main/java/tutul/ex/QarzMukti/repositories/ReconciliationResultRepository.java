package tutul.ex.QarzMukti.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tutul.ex.QarzMukti.entities.ReconciliationReport;
import tutul.ex.QarzMukti.entities.ReconciliationResult;

import java.util.UUID;


import java.util.List;

public interface ReconciliationResultRepository extends JpaRepository<ReconciliationResult, UUID> {
    
    List<ReconciliationResult> findByReport(ReconciliationReport report);
    
    List<ReconciliationResult> findByStatus(String status);
}