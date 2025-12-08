package tutul.ex.QarzMukti.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for system interest rate settings
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemInterestRateResponse {

    private UUID id;
    private BigDecimal baseInterestRate;
    private Instant effectiveFrom;
    private String setByUsername;
    private Long setByUserId;
    private Instant createdAt;
    private Instant updatedAt;
}
