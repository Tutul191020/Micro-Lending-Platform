package tutul.ex.QarzMukti.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVerificationRequest {
    @NotNull(message = "User ID is required")
    private UUID userId;
    
    @NotBlank(message = "Verification status is required")
    @Pattern(regexp = "^(APPROVED|REJECTED)$", message = "Status must be APPROVED or REJECTED")
    private String status;
    
    private String comments;
    
    private Set<String> assignedRoleNames; // Roles to assign on approval
}