package tutul.ex.QarzMukti.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ledger_entries")
@Data
public class LedgerEntry {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String entryType;

    private UUID referenceId;

    private String referenceType;

    @ManyToOne
    @JoinColumn(name = "debit_pool_id")
    private VirtualFundPool debitPool;

    @ManyToOne
    @JoinColumn(name = "credit_pool_id")
    private VirtualFundPool creditPool;

    @Column(nullable = false)
    private BigDecimal amount;

    private String currency = "BDT";

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(columnDefinition = "jsonb")
    private String metadata;
}
