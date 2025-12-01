package tutul.ex.QarzMukti.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInitiationRequest {

    @NotNull(message = "Repayment ID is required")
    private UUID repaymentId;

    @NotNull(message = "Gateway is required (BKASH, NAGAD)")
    private String gateway;

    /**
     * Optional: User can specify custom payment amount for partial payments.
     * If null, defaults to full remaining amount.
     * Must be positive (validated in service layer).
     */
    private BigDecimal amount;
}
