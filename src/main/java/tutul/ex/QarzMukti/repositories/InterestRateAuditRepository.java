package tutul.ex.QarzMukti.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tutul.ex.QarzMukti.entities.InterestRateAudit;

import java.util.UUID;

public interface InterestRateAuditRepository extends JpaRepository<InterestRateAudit, UUID> {}