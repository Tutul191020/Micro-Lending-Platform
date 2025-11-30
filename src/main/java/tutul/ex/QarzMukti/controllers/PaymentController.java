package tutul.ex.QarzMukti.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tutul.ex.QarzMukti.common.ResourceNotFoundException;
import tutul.ex.QarzMukti.dto.ApiResponse;
import tutul.ex.QarzMukti.dto.PaymentInitiationRequest;
import tutul.ex.QarzMukti.dto.PaymentInitiationResponse;
import tutul.ex.QarzMukti.dto.PaymentWebhookRequest;
import tutul.ex.QarzMukti.entities.User;
import tutul.ex.QarzMukti.repositories.UserRepository;
import tutul.ex.QarzMukti.services.PaymentService;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final UserRepository userRepository;

    /**
     * Initiate a loan repayment
     * User initiates this from the frontend
     */
    @PostMapping("/initiate")
    @PreAuthorize("hasAuthority('LOAN:READ')") // Assuming basic user authority covers this
    public ResponseEntity<ApiResponse<PaymentInitiationResponse>> initiatePayment(
            @Valid @RequestBody PaymentInitiationRequest request,
            Authentication authentication) {
        
        User user = getUserFromAuthentication(authentication);
        log.info("Payment initiation request from user: {}", user.getUsername());

        PaymentInitiationResponse response = paymentService.initiateRepayment(user, request);

        return ResponseEntity.ok(ApiResponse.success(response, "Payment initiated successfully"));
    }

    /**
     * Webhook for payment gateway callbacks
     * This endpoint should be secured (e.g., IP whitelisting, signature verification)
     * For mock, we leave it open or use basic checks
     */
    @PostMapping("/webhook")
    public ResponseEntity<ApiResponse<Void>> handlePaymentWebhook(
            @RequestBody PaymentWebhookRequest payload) {
        
        log.info("Received payment webhook: {}", payload);
        paymentService.processPaymentWebhook(payload);

        return ResponseEntity.ok(ApiResponse.success(null, "Webhook processed successfully"));
    }

    private User getUserFromAuthentication(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + authentication.getName()));
    }
}
