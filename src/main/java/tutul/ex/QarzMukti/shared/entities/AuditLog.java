package tutul.ex.QarzMukti.shared.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
@Data
public class AuditLog {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String entityType;

    private UUID entityId;

    @Column(nullable = false)
    private String action;

    @ManyToOne
    @JoinColumn(name = "performed_by")
    private User performedBy;

    @Column(columnDefinition = "jsonb")
    private String payload;

    @Column(nullable = false)
    private Instant createdAt;
}
