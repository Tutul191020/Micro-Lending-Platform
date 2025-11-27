package tutul.ex.QarzMukti.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for user login.
 * Contains credentials required for authentication.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @com.fasterxml.jackson.annotation.JsonAlias({ "Username", "username" })
    private String username;

    @com.fasterxml.jackson.annotation.JsonAlias({ "Email", "email" })
    private String email;

    @NotBlank(message = "Password is required")
    @com.fasterxml.jackson.annotation.JsonAlias({ "Password", "password" })
    private String password;
}
