package tutul.ex.QarzMukti.shared;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tutul.ex.QarzMukti.shared.NotificationService;

/**
 * Console-based implementation of NotificationService for development/testing.
 * Logs OTP codes to System.err (red color in console) instead of sending actual
 * emails/SMS.
 */
@Slf4j
@Service
public class ConsoleNotificationService implements NotificationService {

    @Override
    public void sendEmailOtp(String email, String otpCode) {
        System.err.println("═══════════════════════════════════════════════════════");
        System.err.println("📧 EMAIL OTP NOTIFICATION");
        System.err.println("═══════════════════════════════════════════════════════");
        System.err.println("To: " + email);
        System.err.println("Subject: Password Reset OTP");
        System.err.println("───────────────────────────────────────────────────────");
        System.err.println("Your OTP code is: " + otpCode);
        System.err.println("This code will expire in 10 minutes.");
        System.err.println("═══════════════════════════════════════════════════════");

        log.info("OTP sent to email: {}", maskIdentifier(email));
    }

    @Override
    public void sendSmsOtp(String phone, String otpCode) {
        System.err.println("═══════════════════════════════════════════════════════");
        System.err.println("📱 SMS OTP NOTIFICATION");
        System.err.println("═══════════════════════════════════════════════════════");
        System.err.println("To: " + phone);
        System.err.println("───────────────────────────────────────────────────────");
        System.err.println("Your OTP code is: " + otpCode);
        System.err.println("This code will expire in 10 minutes.");
        System.err.println("═══════════════════════════════════════════════════════");

        log.info("OTP sent to phone: {}", maskIdentifier(phone));
    }

    /**
     * Mask email or phone for privacy in logs.
     */
    private String maskIdentifier(String identifier) {
        if (identifier == null || identifier.length() <= 4) {
            return "***";
        }

        if (identifier.contains("@")) {
            // Email: show first and last char before @
            int atIndex = identifier.indexOf("@");
            String localPart = identifier.substring(0, atIndex);
            String domain = identifier.substring(atIndex);

            if (localPart.length() <= 2) {
                return "***" + domain;
            }
            return localPart.charAt(0) + "***" + localPart.charAt(localPart.length() - 1) + domain;
        } else {
            // Phone: show last 4 digits
            return "***" + identifier.substring(identifier.length() - 4);
        }
    }
}
