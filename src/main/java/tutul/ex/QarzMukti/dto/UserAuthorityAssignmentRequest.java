package tutul.ex.QarzMukti.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class UserAuthorityAssignmentRequest {
    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotEmpty(message = "At least one authority is required")
    private Set<String> authorityNames;
}