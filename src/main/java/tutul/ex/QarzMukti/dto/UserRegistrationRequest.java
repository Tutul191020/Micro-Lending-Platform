package tutul.ex.QarzMukti.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationRequest {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Username can only contain letters, numbers, underscore and hyphen")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).*$", 
             message = "Password must contain at least one digit, one lowercase, one uppercase, and one special character")
    private String password;

    @NotBlank(message = "Full name is required")
    @Size(max = 100)
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^(\\+88)?01[3-9]\\d{8}$", message = "Invalid Bangladesh phone number")
    private String phone;

    @NotBlank(message = "National ID is required")
    @Pattern(regexp = "^[0-9]{10,17}$", message = "Invalid National ID format")
    private String nationalId;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Address is required")
    @Size(max = 500)
    private String address;

    @Size(max = 100)
    private String city;

    @Size(max = 20)
    private String postalCode;

    // Document uploads - will be handled separately via multipart
    // These fields store the file paths/URLs after upload
    private String nationalIdDocumentPath;
    private String electricityBillDocumentPath;
    private String profilePhotoPath;
}