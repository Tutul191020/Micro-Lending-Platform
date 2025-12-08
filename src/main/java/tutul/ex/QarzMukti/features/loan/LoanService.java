package tutul.ex.QarzMukti.features.loan;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tutul.ex.QarzMukti.shared.common.ValidationException;
import tutul.ex.QarzMukti.shared.entities.User;
import tutul.ex.QarzMukti.shared.enums.OnboardingStatus;
import tutul.ex.QarzMukti.shared.repositories.UserRepository;
import tutul.ex.QarzMukti.features.user.UserService;

/**
 * Example Loan Service demonstrating loan eligibility validation
 * This is a placeholder service showing how to integrate onboarding status
 * checks
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoanService {

    private final UserRepository userRepository;
    private final UserService userService;

    /**
     * Example method showing how to validate loan eligibility before processing
     * loan application
     * 
     * @param userId The user applying for the loan
     * @throws ValidationException if user is not eligible for loan
     */
    public void validateLoanEligibility(Long userId) {
        log.info("Validating loan eligibility for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ValidationException("User not found: " + userId));

        // Check if user is eligible for loan using the UserService method
        Boolean isEligible = userService.checkLoanEligibility(userId);

        if (!isEligible) {
            // Build detailed error message
            String errorMessage = buildIneligibilityMessage(user);
            log.warn("User {} is not eligible for loan: {}", userId, errorMessage);
            throw new ValidationException(errorMessage);
        }

        log.info("User {} is eligible for loan application", userId);
    }

    /**
     * Build a detailed message explaining why user is not eligible
     */
    private String buildIneligibilityMessage(User user) {
        StringBuilder message = new StringBuilder("User is not eligible for loan application. ");

        if (user.getOnboardingStatus() != OnboardingStatus.ONBOARDING_COMPLETED) {
            message.append("Reason: Onboarding not completed. ");
            message.append("Current status: ").append(user.getOnboardingStatus()).append(". ");
            message.append("Please upload all required documents (National ID, Electricity Bill, Profile Photo). ");
        } else if (!"APPROVED".equals(user.getVerificationStatus()) || !user.getVerified()) {
            message.append("Reason: Account not verified by admin. ");
            message.append("Current verification status: ").append(user.getVerificationStatus()).append(". ");
            message.append("Please wait for admin approval. ");
        } else if (!user.getEnabled()) {
            message.append("Reason: Account is disabled. ");
            message.append("Please contact support for assistance. ");
        } else {
            message.append("Please ensure all requirements are met. ");
        }

        return message.toString();
    }

    /**
     * Example placeholder method for loan application
     * In a real implementation, this would create a loan record
     * 
     * @param userId The user applying for the loan
     * @return Success message
     */
    public String applyForLoan(Long userId) {
        // First validate eligibility
        validateLoanEligibility(userId);

        // If validation passes, proceed with loan application
        log.info("Processing loan application for user: {}", userId);

        // TODO: Implement actual loan application logic here
        // - Create loan record
        // - Set initial status
        // - Trigger approval workflow
        // - Send notifications

        return "Loan application submitted successfully. Your application is under review.";
    }
}
