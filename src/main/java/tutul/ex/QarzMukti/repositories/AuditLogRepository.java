package tutul.ex.QarzMukti.repositories;

import org.hibernate.validator.constraints.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import tutul.ex.QarzMukti.entities.AuditLog;



public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {}