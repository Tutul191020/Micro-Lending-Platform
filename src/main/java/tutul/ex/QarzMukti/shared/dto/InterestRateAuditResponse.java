package tutul.ex.QarzMukti.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterestRateAuditResponse {

    private UUID id;
    private UUID loanId;
    private BigDecimal oldInterestRate;
    private BigDecimal newInterestRate;
    private String changedByUsername;
    private Long changedByUserId;
    private String changeReason;
    private Instant createdAt;
    private Instant updatedAt;
}
