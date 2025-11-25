package tutul.ex.QarzMukti.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment_transactions",
       uniqueConstraints = @UniqueConstraint(columnNames = {"gateway", "gatewayTxId"}))
@Data
public class PaymentTransaction {

    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "repayment_id")
    private LoanRepayment repayment;

    @ManyToOne
    @JoinColumn(name = "loan_id")
    private Loan loan;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String gateway;

    private String gatewayTxId;

    private UUID idempotencyKey;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private Instant initiatedAt;

    private Instant completedAt;

    @Column(columnDefinition = "jsonb")
    private String metadata;
}
