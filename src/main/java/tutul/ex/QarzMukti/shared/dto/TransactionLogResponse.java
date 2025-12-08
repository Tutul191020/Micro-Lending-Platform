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
public class TransactionLogResponse {
    private UUID id;
    private String transactionType;
    private BigDecimal amount;
    private Long fromUserId;
    private String fromUsername;
    private Long toUserId;
    private String toUsername;
    private UUID referenceId;
    private String referenceType;
    private String description;
    private Long performedById;
    private String performedByUsername;
    private Instant createdAt;
}
