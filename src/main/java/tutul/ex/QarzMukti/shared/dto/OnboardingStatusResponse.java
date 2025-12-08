package tutul.ex.QarzMukti.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tutul.ex.QarzMukti.shared.enums.OnboardingStatus;

import java.time.OffsetDateTime;

/**
 * Response DTO for user onboarding status and progress
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnboardingStatusResponse {
    
    private Long userId;
    private String username;
    private String fullName;
    private OnboardingStatus onboardingStatus;
    private DocumentCompletionStatus documentStatus;
    private Boolean isEligibleForLoan;
    private String eligibilityMessage;
    private OffsetDateTime onboardingCompletedAt;
    
    /**
     * Nested DTO for document completion status
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocumentCompletionStatus {
        private Boolean nationalIdUploaded;
        private Boolean electricityBillUploaded;
        private Boolean profilePhotoUploaded;
        private Integer totalRequired;
        private Integer totalUploaded;
        private Double completionPercentage;
    }
}
