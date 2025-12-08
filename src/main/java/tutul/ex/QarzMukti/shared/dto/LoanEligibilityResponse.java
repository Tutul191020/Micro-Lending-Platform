package tutul.ex.QarzMukti.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for loan eligibility check
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanEligibilityResponse {
    
    private Long userId;
    private Boolean isEligible;
    private String message;
    private EligibilityDetails details;
    
    /**
     * Detailed breakdown of eligibility criteria
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EligibilityDetails {
        private Boolean onboardingCompleted;
        private Boolean documentsUploaded;
        private Boolean adminVerified;
        private Boolean accountEnabled;
        private String currentOnboardingStatus;
        private String currentVerificationStatus;
    }
}
