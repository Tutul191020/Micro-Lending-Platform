package tutul.ex.QarzMukti.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleAuthorityAssignmentRequest {
    @NotNull(message = "Role ID is required")
    private Integer roleId;

    @NotEmpty(message = "At least one authority is required")
    private Set<String> authorityNames;
}