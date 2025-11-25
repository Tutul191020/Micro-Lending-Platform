package tutul.ex.QarzMukti.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "reconciliation_reports")
@Data
public class ReconciliationReport {

    @Id
    private UUID id;

    @Column(nullable = false)
    private LocalDate reportDate;

    @Column(nullable = false)
    private String provider;

    @Column(columnDefinition = "jsonb", nullable = false)
    private String rawReport;

    @Column(nullable = false)
    private Instant createdAt;
}
