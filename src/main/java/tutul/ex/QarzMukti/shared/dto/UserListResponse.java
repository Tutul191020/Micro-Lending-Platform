package tutul.ex.QarzMukti.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tutul.ex.QarzMukti.shared.enums.OnboardingStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserListResponse {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private Boolean enabled;
    private Boolean verified;
    private String verificationStatus;
    private OnboardingStatus onboardingStatus;

}