package tutul.ex.QarzMukti.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for setting or updating the system-wide default interest rate
 * Only SuperAdmin can perform this operation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemInterestRateRequest {

    @NotNull(message = "Interest rate is required")
    @DecimalMin(value = "0.01", message = "Interest rate must be greater than 0")
    private BigDecimal interestRate;

    private String reason; // Optional reason for the rate change
}
