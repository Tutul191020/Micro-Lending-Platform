package tutul.ex.QarzMukti.shared.entities;

import jakarta.persistence.*;
import jdk.jfr.Timestamp;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;


@Entity
@Table(name = "interest_rate_audit")
@Data
public class InterestRateAudit {

    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "loan_id")
    private Loan loan;

    private BigDecimal oldInterestRate;

    private BigDecimal newInterestRate;

    @ManyToOne
    @JoinColumn(name = "changed_by_user_id")
    private User changedBy;

    private String changeReason;

    @Column(nullable = false)
    @CreationTimestamp
    private Instant createdAt;

     @Column(nullable = false)
    private Instant updatedAt;
}
