package tutul.ex.QarzMukti.shared.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tutul.ex.QarzMukti.shared.enums.InterestType;

import java.math.BigDecimal;

/**
 * Request DTO for loan application
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplicationRequest {

    @NotNull(message = "Principal amount is required")
    @DecimalMin(value = "1000.00", message = "Minimum loan amount is 1000 BDT")
    private BigDecimal principal;

    @NotNull(message = "Tenure is required")
    @Min(value = 1, message = "Minimum tenure is 1 month")
    private Integer tenureMonths;

    @NotNull(message = "Interest type is required")
    private InterestType interestType;

    private String purpose; // Optional: reason for loan
}
