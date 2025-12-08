package tutul.ex.QarzMukti.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInitiationResponse {

    private UUID transactionId;
    private String gatewayUrl;
    private String status;
    private String gateway;
}
