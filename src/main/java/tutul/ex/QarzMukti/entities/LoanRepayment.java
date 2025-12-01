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

    // Track original amount before any deficit/excess adjustments
    private BigDecimal originalTotalDue;

    // Flag to prevent consecutive underpayments
    private Boolean hasDeficitFromPreviousMonth = false;

    @Column(nullable = false)
    private String status;

    private Instant paidAt;

    @Column(nullable = false)
    private Instant createdAt;

    // Helper methods for partial payment logic
    public BigDecimal getRemainingAmount() {
        return totalDue.subtract(paidAmount);
    }

    public boolean isFullyPaid() {
        return paidAmount.compareTo(totalDue) >= 0;
    }

    public boolean isPartiallyPaid() {
        return paidAmount.compareTo(BigDecimal.ZERO) > 0
                && paidAmount.compareTo(totalDue) < 0;
    }

    public boolean isWithinCurrentMonth() {
        LocalDate now = LocalDate.now();
        return dueDate.getYear() == now.getYear()
                && dueDate.getMonth() == now.getMonth();
    }

    public BigDecimal getMinimumPaymentRequired() {
        // If has deficit from previous month, must pay at least original amount
        if (Boolean.TRUE.equals(hasDeficitFromPreviousMonth) && originalTotalDue != null) {
            return originalTotalDue;
        }
        // Otherwise, no minimum (or use configured minimum %)
        return BigDecimal.ZERO;
    }
}
