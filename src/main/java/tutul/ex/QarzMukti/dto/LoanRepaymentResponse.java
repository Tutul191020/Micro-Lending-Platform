package tutul.ex.QarzMukti.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Response DTO for loan repayment schedule
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanRepaymentResponse {

    private UUID id;
    private UUID loanId;
    private LocalDate dueDate;
    private BigDecimal principalDue;
    private BigDecimal interestDue;
    private BigDecimal feeDue;
    private BigDecimal totalDue;
    private BigDecimal paidAmount;
    private String status; // PENDING, PAID, OVERDUE, PARTIAL
    private Instant paidAt;
    private Instant createdAt;
}
