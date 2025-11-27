package tutul.ex.QarzMukti.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserListResponse {

    private String username;
    private String fullName;
    private String email;
    private String phone;
    private Boolean enabled;
    private Boolean verified;
    private String verificationStatus;

}