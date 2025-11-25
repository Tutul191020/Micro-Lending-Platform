package tutul.ex.QarzMukti.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "loan_repayments")
@Data
public class LoanRepayment {

    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "loan_id")
    private Loan loan;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Column(nullable = false)
    private BigDecimal principalDue;

    @Column(nullable = false)
    private BigDecimal interestDue;

    private BigDecimal feeDue = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal totalDue;

    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    private String status;

    private Instant paidAt;

    @Column(nullable = false)
    private Instant createdAt;
}
