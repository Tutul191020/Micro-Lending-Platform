package tutul.ex.QarzMukti.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserListResponse {
    private UUID id;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private Boolean enabled;
    private Boolean verified;
    private String verificationStatus;
    private Set<String> roleNames;
    private OffsetDateTime createdAt;
}