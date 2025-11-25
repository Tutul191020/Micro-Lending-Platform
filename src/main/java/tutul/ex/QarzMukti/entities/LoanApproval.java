package tutul.ex.QarzMukti.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "loan_approvals")
@Data
public class LoanApproval {

    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "loan_id")
    private Loan loan;

    @ManyToOne
    @JoinColumn(name = "assigned_accountant_id")
    private User assignedAccountant;

    private BigDecimal proposedInterestRate;

    @Column(nullable = false)
    private String status;

    private String decisionComment;

    private Instant decidedAt;

    @Column(nullable = false)
    private Instant createdAt;
}
