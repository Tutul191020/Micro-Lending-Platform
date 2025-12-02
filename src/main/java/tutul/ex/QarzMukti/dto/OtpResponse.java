package tutul.ex.QarzMukti.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for OTP-related operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpResponse {

    private String message;
    private Integer expiresIn; // seconds until expiry
    private String otpSentTo; // masked email/phone
}
