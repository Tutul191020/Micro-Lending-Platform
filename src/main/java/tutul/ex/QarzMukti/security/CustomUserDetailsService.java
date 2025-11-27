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
import tutul.ex.QarzMukti.entities.UserRole;
import tutul.ex.QarzMukti.entities.UserAuthority;
import tutul.ex.QarzMukti.repositories.UserRepository;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with username: " + username));

        // Check if user is enabled and verified
        if (!user.getEnabled()) {
            throw new UsernameNotFoundException("User account is disabled");
        }

        if (!user.getVerified()) {
            throw new UsernameNotFoundException("User account is not verified");
        }

        Set<GrantedAuthority> authorities = getAuthorities(user);

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(!user.getEnabled())
                .credentialsExpired(false)
                .disabled(!user.getEnabled())
                .build();
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
