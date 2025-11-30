package tutul.ex.QarzMukti.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tutul.ex.QarzMukti.entities.ReconciliationReport;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ReconciliationReportRepository extends JpaRepository<ReconciliationReport, UUID> {
    
    List<ReconciliationReport> findByReportDateOrderByCreatedAtDesc(LocalDate reportDate);
    
    List<ReconciliationReport> findAllByOrderByCreatedAtDesc();
}