package tutul.ex.QarzMukti.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private LocalDate dateOfBirth;
    private Boolean enabled;
    private String verificationStatus;
    private Long verifiedBy;
    private Set<AuthorityResponse> authorities;
    private OffsetDateTime createdAt;

}