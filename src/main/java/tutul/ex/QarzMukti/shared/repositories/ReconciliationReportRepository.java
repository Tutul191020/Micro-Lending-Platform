package tutul.ex.QarzMukti.shared.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tutul.ex.QarzMukti.shared.entities.ReconciliationReport;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ReconciliationReportRepository extends JpaRepository<ReconciliationReport, UUID> {
    
    List<ReconciliationReport> findByReportDateOrderByCreatedAtDesc(LocalDate reportDate);
    
    List<ReconciliationReport> findAllByOrderByCreatedAtDesc();
}