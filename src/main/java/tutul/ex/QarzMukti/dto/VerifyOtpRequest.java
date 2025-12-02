package tutul.ex.QarzMukti.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for verifying OTP code.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyOtpRequest {

    @NotBlank(message = "Email or phone number is required")
    private String identifier;

    @NotBlank(message = "OTP code is required")
    @Size(min = 4, max = 4, message = "OTP code must be 4 digits")
    @Pattern(regexp = "^[0-9]{4}$", message = "OTP code must be 4 digits")
    private String otpCode;
}
