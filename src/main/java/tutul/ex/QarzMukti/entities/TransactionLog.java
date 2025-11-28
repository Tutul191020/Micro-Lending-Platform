package tutul.ex.QarzMukti.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transaction_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionLog {

    @Id
    private UUID id;

    @Column(nullable = false, length = 50)
    private String transactionType; // FUND_MINT, FUND_ALLOCATION, FUND_REQUEST, LOAN_DISBURSEMENT, REPAYMENT

    @Column(nullable = false)
    private BigDecimal amount;

    @ManyToOne
    @JoinColumn(name = "from_user_id")
    private User fromUser;

    @ManyToOne
    @JoinColumn(name = "to_user_id")
    private User toUser;

    private UUID fromPoolId;

    private UUID toPoolId;

    private UUID referenceId; // Links to FundRequest, Loan, Payment, etc.

    @Column(length = 50)
    private String referenceType; // FUND_REQUEST, LOAN, PAYMENT, etc.

    @Column(nullable = false, length = 500)
    private String description;

    @Column(columnDefinition = "jsonb")
    private String metadata;

    @ManyToOne
    @JoinColumn(name = "performed_by", nullable = false)
    private User performedBy;

    @Column(nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
