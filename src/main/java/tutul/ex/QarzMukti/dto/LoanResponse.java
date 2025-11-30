package tutul.ex.QarzMukti.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for loan details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanResponse {

    private UUID id;
    private Long userId;
    private String username;
    private String userFullName;
    private BigDecimal principal;
    private BigDecimal approvedInterestRate;
    private String interestType;
    private Integer tenureMonths;
    private String status;
    private BigDecimal outstandingAmount;
    private String currency;
    private Long approvedByUserId;
    private String approvedByUsername;
    private Instant approvedAt;
    private Instant disbursedAt;
    private Instant createdAt;
    private Long version;
}
