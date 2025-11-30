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
 * Request DTO for SuperAdmin to override interest rate for a specific loan
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanInterestRateOverrideRequest {

    @NotNull(message = "Loan ID is required")
    private UUID loanId;

    @NotNull(message = "Interest rate is required")
    @DecimalMin(value = "0.01", message = "Interest rate must be greater than 0")
    private BigDecimal interestRate;

    @NotNull(message = "Reason for override is required")
    private String reason;
}
