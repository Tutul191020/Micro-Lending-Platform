package tutul.ex.QarzMukti.shared;

/**
 * Service interface for sending notifications (Email/SMS).
 */
public interface NotificationService {

    /**
     * Send OTP via email.
     * 
     * @param email   Recipient email address
     * @param otpCode OTP code to send
     */
    void sendEmailOtp(String email, String otpCode);

    /**
     * Send OTP via SMS.
     * 
     * @param phone   Recipient phone number
     * @param otpCode OTP code to send
     */
    void sendSmsOtp(String phone, String otpCode);
}
