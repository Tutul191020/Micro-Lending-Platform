package tutul.ex.QarzMukti.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tutul.ex.QarzMukti.entities.User;
import tutul.ex.QarzMukti.repositories.UserRepository;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        log.debug("Loading user by username or email: {}", usernameOrEmail);

        // Try to find by username first, then by email
        User user = userRepository.findByUsername(usernameOrEmail)
                .orElseGet(() -> userRepository.findByEmail(usernameOrEmail)
                        .orElseThrow(() -> new UsernameNotFoundException(
                                "User not found with username or email: " + usernameOrEmail)));

        // Check if user is enabled and verified
        if (!user.getEnabled()) {
            throw new UsernameNotFoundException("User account is disabled");
        }

        if (!user.getVerified()) {
            throw new UsernameNotFoundException("User account is not verified");
        }

        Set<GrantedAuthority> authorities = getAuthorities(user);

        return new CustomUserDetails(user, authorities);
    }

    /**
     * Get all authorities for a user (from roles and direct assignments).
     */
    private Set<GrantedAuthority> getAuthorities(User user) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        // Add role-based authorities
        // Note: In a complete implementation, you would fetch UserRole entities
        // For now, we'll add a placeholder. This will be enhanced when UserRole
        // repository is available.

        // Add direct authorities
        // Note: Similar to roles, this would fetch UserAuthority entities
        // For now, adding a default authority
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        log.debug("User {} has authorities: {}", user.getUsername(), authorities);

        return authorities;
    }
}
