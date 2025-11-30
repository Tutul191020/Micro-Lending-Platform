package tutul.ex.QarzMukti.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request DTO for SuperAdmin to recalculate loan repayment schedule with new interest rate
 * This is used for mid-loan interest rate adjustments
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanRecalculationRequest {

    @NotNull(message = "Loan ID is required")
    private UUID loanId;

    @NotNull(message = "New interest rate is required")
    @DecimalMin(value = "0.01", message = "Interest rate must be greater than 0")
    private BigDecimal newInterestRate;

    @NotNull(message = "Reason for recalculation is required")
    private String reason;
}
