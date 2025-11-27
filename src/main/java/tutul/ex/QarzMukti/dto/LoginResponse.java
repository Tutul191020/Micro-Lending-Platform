package tutul.ex.QarzMukti.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for successful login.
 * Contains JWT access token and user information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String accessToken;

    private String tokenType;

    private Long expiresIn; // in seconds

    /**
     * Creates a login response with Bearer token type.
     */
    public static LoginResponse of(String accessToken, Long expiresIn) {
        return LoginResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .build();
    }
}
