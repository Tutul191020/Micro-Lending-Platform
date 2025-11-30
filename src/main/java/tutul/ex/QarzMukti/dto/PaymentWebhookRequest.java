package tutul.ex.QarzMukti.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentWebhookRequest {

    private UUID paymentId;
    private String status; // SUCCESS, FAILED
    private String gatewayTxId;
}
