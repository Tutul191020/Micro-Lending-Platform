package tutul.ex.QarzMukti.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tutul.ex.QarzMukti.dto.PaymentInitiationResponse;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Simulates external payment gateways like bKash, Nagad, etc.
 */
@Slf4j
@Service
public class MockPaymentGatewayService {

    private static final String MOCK_GATEWAY_BASE_URL = "http://localhost:3000/mock-payment";

    /**
     * Initiates a payment request to the mock gateway
     */
    public PaymentInitiationResponse initiatePayment(UUID transactionId, BigDecimal amount, String gateway) {
        log.info("Initiating mock payment: txId={}, amount={}, gateway={}", transactionId, amount, gateway);

        // In a real scenario, we would make an HTTP call to the gateway's API
        // Here we just generate a mock URL where the frontend would redirect the user
        
        String gatewayUrl = String.format("%s?txId=%s&amount=%s&gateway=%s", 
                MOCK_GATEWAY_BASE_URL, transactionId, amount, gateway);

        return PaymentInitiationResponse.builder()
                .transactionId(transactionId)
                .gatewayUrl(gatewayUrl)
                .status("INITIATED")
                .gateway(gateway)
                .build();
    }
}
