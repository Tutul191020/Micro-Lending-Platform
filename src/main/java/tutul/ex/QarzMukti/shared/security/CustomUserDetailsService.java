package tutul.ex.QarzMukti.shared.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tutul.ex.QarzMukti.shared.entities.User;
import tutul.ex.QarzMukti.shared.entities.Role;
import tutul.ex.QarzMukti.shared.entities.Authority;
import tutul.ex.QarzMukti.shared.repositories.RoleAuthorityRepository;
import tutul.ex.QarzMukti.shared.repositories.UserAuthorityRepository;
import tutul.ex.QarzMukti.shared.repositories.UserRepository;
import tutul.ex.QarzMukti.shared.repositories.UserRoleRepository;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserAuthorityRepository userAuthorityRepository;
    private final RoleAuthorityRepository roleAuthorityRepository;

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
        // 1. Get roles
        Set<Role> roles = userRoleRepository.findRolesByUserId(user.getId());
        for (Role role : roles) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));

            // Fetch authorities associated with this role
            Set<Authority> roleAuthorities = roleAuthorityRepository
                    .findAuthoritiesByRoleId(role.getId());
            for (Authority authority : roleAuthorities) {
                authorities.add(new SimpleGrantedAuthority(authority.getName()));
            }
        }

        // 2. Get direct authorities
        Set<Authority> userAuthorities = userAuthorityRepository
                .findAllUserAuthorities(user.getId());
        for (Authority authority : userAuthorities) {
            authorities.add(new SimpleGrantedAuthority(authority.getName()));
        }

        // Ensure ROLE_USER is always present if no other roles are assigned, or just as
        // a base role
        // But if user has roles, we might not want to force it unless it's a business
        // rule.
        // For now, keeping it as it was requested to have "all permissions" for
        // superadmin.
        // If the user has roles, they will be added above.

        log.debug("User {} has authorities: {}", user.getUsername(), authorities);

        return authorities;
    }
}
