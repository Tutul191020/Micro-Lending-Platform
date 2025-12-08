package tutul.ex.QarzMukti.shared.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment_allocations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentAllocation {

    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "transaction_id", nullable = false)
    private PaymentTransaction transaction;

    @ManyToOne
    @JoinColumn(name = "repayment_id", nullable = false)
    private LoanRepayment repayment;

    @Column(nullable = false)
    private BigDecimal allocatedAmount;

    /**
     * Allocation type:
     * - CURRENT_INSTALLMENT: Payment applied to the target installment for the
     * current month
     * - ADVANCE_PAYMENT: Overpayment distributed to future installments (paying
     * ahead)
     * - DEFICIT_TRANSFER: Underpayment deficit transferred to next month's
     * installment
     */
    @Column(nullable = false)
    private String allocationType;

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
