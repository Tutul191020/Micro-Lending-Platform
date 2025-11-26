package tutul.ex.QarzMukti.dto;

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
public class AuthorityRequest {
    @NotBlank(message = "Authority name is required")
    @Pattern(regexp = "^[A-Z_:]+$", message = "Authority name must be uppercase with underscores and colons")
    private String name;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    @NotBlank(message = "Resource is required")
    private String resource;
    
    @NotBlank(message = "Action is required")
    private String action;
}