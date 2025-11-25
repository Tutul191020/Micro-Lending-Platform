package tutul.ex.QarzMukti.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "loans")
@Data
public class Loan {

    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private BigDecimal principal;

    @Column(nullable = false)
    private BigDecimal approvedInterestRate;

    @Column(nullable = false)
    private String interestType;

    @Column(nullable = false)
    private Integer tenureMonths;

    @Column(nullable = false)
    private String status;

    private BigDecimal outstandingAmount;

    private String currency = "BDT";

    @Column(nullable = false)
    private Long version;

    private Instant disbursedAt;

    @Column(nullable = false)
    private Instant createdAt;

    @ManyToOne
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    private Instant approvedAt;
}
