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
    
    // Optional: if partial payment is allowed in future
    private BigDecimal amount;
}
