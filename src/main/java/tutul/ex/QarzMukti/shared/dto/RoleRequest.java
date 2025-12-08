package tutul.ex.QarzMukti.shared.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleRequest {
    @NotBlank(message = "Role name is required")
    @Pattern(regexp = "^[A-Z_]+$", message = "Role name must be uppercase with underscores only")
    private String name;
    
    private String description;
}