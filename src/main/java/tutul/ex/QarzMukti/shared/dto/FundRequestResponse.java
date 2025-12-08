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
public class FundRequestResponse {

    private UUID id;
    private BigDecimal amount;
    private String reason;
    private String status;
    private Long requestedById;
    private String requestedByFullName;
    private Long reviewedById;
    private String reviewedByUsername;
    private Instant reviewedAt;
    private String reviewComments;
    private Instant createdAt;
}
