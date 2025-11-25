package tutul.ex.QarzMukti.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reconciliation_results")
@Data
public class ReconciliationResult {

    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "reconciliation_report_id")
    private ReconciliationReport report;

    private String gatewayTxId;

    @ManyToOne
    @JoinColumn(name = "matched_payment_transaction_id")
    private PaymentTransaction matchedTransaction;

    @Column(nullable = false)
    private String status;

    @Column(columnDefinition = "jsonb")
    private String details;

    @Column(nullable = false)
    private Instant processedAt;
}
