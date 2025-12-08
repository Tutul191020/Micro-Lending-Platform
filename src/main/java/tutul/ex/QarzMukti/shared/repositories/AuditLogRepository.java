package tutul.ex.QarzMukti.shared.repositories;

import org.hibernate.validator.constraints.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import tutul.ex.QarzMukti.shared.entities.AuditLog;



public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {}