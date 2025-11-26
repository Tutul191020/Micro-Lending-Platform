package tutul.ex.QarzMukti.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchCriteria {
    private String username;
    private String email;
    private String fullName;
    private String nationalId;
    private Boolean enabled;
    private Boolean verified;
    private String verificationStatus; // PENDING, APPROVED, REJECTED
    private Set<String> roleNames;
    
    @Min(0)
    private Integer page = 0;
    
    @Min(1)
    @Max(100)
    private Integer size = 20;
    
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
}