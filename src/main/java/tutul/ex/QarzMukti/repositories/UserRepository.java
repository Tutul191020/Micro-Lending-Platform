package tutul.ex.QarzMukti.repositories;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import tutul.ex.QarzMukti.entities.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

        boolean existsByEmail(@Email(message = "Invalid email format") String email);

        boolean existsByUsername(
                        @NotBlank(message = "Username is required") @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters") @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Username can only contain letters, numbers, underscore and hyphen") String username);

        boolean existsByNationalId(
                        @NotBlank(message = "National ID is required") @Pattern(regexp = "^[0-9]{10,17}$", message = "Invalid National ID format") String nationalId);

        List<User> findByVerificationStatus(String pending);

        Optional<User> findByUsername(String username);

        Optional<User> findByEmail(String email);

        Optional<User> findByPhone(String phone);
}