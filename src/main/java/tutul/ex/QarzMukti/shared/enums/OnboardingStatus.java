package tutul.ex.QarzMukti.shared.enums;

public enum OnboardingStatus {
    
    /**
     * User has just registered but hasn't uploaded any documents yet
     */
    REGISTERED,
    
    /**
     * User has started uploading documents (at least 1 document uploaded)
     */
    DOCUMENTS_PENDING,
    
    /**
     * All required documents have been uploaded
     * (National ID, Electricity Bill, Profile Photo)
     */
    DOCUMENTS_COMPLETED,
    
    /**
     * User has completed the entire onboarding process
     * All documents uploaded and profile is complete
     * User is ready for admin verification
     */
    ONBOARDING_COMPLETED
}
