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
    private String phone;
    private String nationalId;
    private LocalDate dateOfBirth;
    private String address;
    private String city;
    private String postalCode;
    private Boolean enabled;
    private Boolean verified;
    private String verificationStatus;
    private String verificationComments;
    private String verifiedBy;
    private OffsetDateTime verifiedAt;
    private Set<RoleResponse> roles;
    private Set<AuthorityResponse> authorities;
    private UserDocumentsResponse documents;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}