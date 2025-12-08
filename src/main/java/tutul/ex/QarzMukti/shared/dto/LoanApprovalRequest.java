package tutul.ex.QarzMukti.shared.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request DTO for loan approval/rejection by accountant
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApprovalRequest {

    @NotNull(message = "Loan ID is required")
    private UUID loanId;

    @NotNull(message = "Decision is required (APPROVED or REJECTED)")
    private String decision; // "APPROVED" or "REJECTED"

    @DecimalMin(value = "0.01", message = "Interest rate must be greater than 0")
    private BigDecimal overrideInterestRate; // Optional: override system default

    @NotNull
    private String comment; // Optional: approval/rejection comment
}
