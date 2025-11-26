package tutul.ex.QarzMukti.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tutul.ex.QarzMukti.common.DuplicateResourceException;
import tutul.ex.QarzMukti.common.ForbiddenException;
import tutul.ex.QarzMukti.common.ResourceNotFoundException;
import tutul.ex.QarzMukti.common.ValidationException;
import tutul.ex.QarzMukti.dto.AuthorityRequest;
import tutul.ex.QarzMukti.dto.AuthorityResponse;
import tutul.ex.QarzMukti.dto.RoleAuthorityAssignmentRequest;
import tutul.ex.QarzMukti.entities.Authority;
import tutul.ex.QarzMukti.entities.Role;
import tutul.ex.QarzMukti.entities.RoleAuthority;
import tutul.ex.QarzMukti.repositories.AuthorityRepository;
import tutul.ex.QarzMukti.repositories.RoleAuthorityRepository;
import tutul.ex.QarzMukti.repositories.UserAuthorityRepository;
import tutul.ex.QarzMukti.repositories.UserRoleRepository;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorityService {

    private final AuthorityRepository authorityRepository;
    private final RoleAuthorityRepository roleAuthorityRepository;
    private final UserAuthorityRepository userAuthorityRepository;
    private final UserRoleRepository userRoleRepository;

    /**
     * Create a new authority (SUPER_USER only)
     */
    @Transactional
    public AuthorityResponse createAuthority(Long superUserId, AuthorityRequest request) {
        log.info("Creating new authority: {} by super user: {}", request.getName(), superUserId);

        validateSuperUser(superUserId);

        if (authorityRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Authority already exists: " + request.getName());
        }

        Authority authority = Authority.builder()
                .name(request.getName())
                .description(request.getDescription())
                .resource(request.getResource())
                .action(request.getAction())
                .createdAt(OffsetDateTime.now())
                .build();

        authority = authorityRepository.save(authority);

        log.info("Authority created successfully: {}", authority.getId());
        return mapToAuthorityResponse(authority);
    }

    /**
     * Get all authorities
     */
    @Transactional(readOnly = true)
    public List<AuthorityResponse> getAllAuthorities() {
        return authorityRepository.findAllOrderedByResourceAndAction().stream()
                .map(this::mapToAuthorityResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get authority by ID
     */
    @Transactional(readOnly = true)
    public AuthorityResponse getAuthorityById(Integer authorityId) {
        Authority authority = findAuthorityOrThrow(authorityId);
        return mapToAuthorityResponse(authority);
    }

    /**
     * Get authority by name
     */
    @Transactional(readOnly = true)
    public AuthorityResponse getAuthorityByName(String name) {
        Authority authority = authorityRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Authority not found: " + name));
        return mapToAuthorityResponse(authority);
    }

    /**
     * Get authorities by resource
     */
    @Transactional(readOnly = true)
    public List<AuthorityResponse> getAuthoritiesByResource(String resource) {
        return authorityRepository.findByResource(resource).stream()
                .map(this::mapToAuthorityResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update authority (SUPER_USER only)
     */
    @Transactional
    public AuthorityResponse updateAuthority(Long superUserId, Integer authorityId, AuthorityRequest request) {
        log.info("Updating authority: {} by super user: {}", authorityId, superUserId);

        validateSuperUser(superUserId);
        Authority authority = findAuthorityOrThrow(authorityId);

        if (!authority.getName().equals(request.getName())) {
            if (authorityRepository.existsByName(request.getName())) {
                throw new DuplicateResourceException("Authority name already exists: " + request.getName());
            }
            authority.setName(request.getName());
        }

        authority.setDescription(request.getDescription());
        authority.setResource(request.getResource());
        authority.setAction(request.getAction());

        authority = authorityRepository.save(authority);

        log.info("Authority updated successfully: {}", authorityId);
        return mapToAuthorityResponse(authority);
    }

    /**
     * Delete authority (SUPER_USER only)
     */
    @Transactional
    public void deleteAuthority(Long superUserId, Integer authorityId) {
        log.info("Deleting authority: {} by super user: {}", authorityId, superUserId);

        validateSuperUser(superUserId);
        Authority authority = findAuthorityOrThrow(authorityId);

        // Check if any roles or users have this authority
        long roleCount = roleAuthorityRepository.countByAuthorityId(authorityId);
        long userCount = userAuthorityRepository.countByAuthorityId(authorityId);

        if (roleCount > 0 || userCount > 0) {
            throw new ValidationException(
                    String.format("Cannot delete authority. %d role(s) and %d user(s) are using this authority.",
                            roleCount, userCount)
            );
        }

        authorityRepository.delete(authority);
        log.info("Authority deleted successfully: {}", authorityId);
    }

    /**
     * Assign authorities to role (SUPER_USER only)
     */
    @Transactional
    public void assignAuthoritiesToRole(Long superUserId, RoleAuthorityAssignmentRequest request) {
        log.info("Assigning authorities to role: {} by super user: {}", request.getRoleId(), superUserId);

        validateSuperUser(superUserId);
        Set<Authority> authorities = validateAndFetchAuthorities(request.getAuthorityNames());

        // Remove existing authorities
        roleAuthorityRepository.deleteByRoleId(request.getRoleId());

        // Assign new authorities
        for (Authority authority : authorities) {
            RoleAuthority roleAuthority = RoleAuthority.builder()
                    .roleId(request.getRoleId())
                    .authorityId(authority.getId())
                    .assignedBy(superUserId)
                    .assignedAt(OffsetDateTime.now())
                    .build();
            roleAuthorityRepository.save(roleAuthority);
        }

        log.info("Authorities assigned successfully to role: {}", request.getRoleId());
    }

    /**
     * Get all authorities for a role
     */
    @Transactional(readOnly = true)
    public Set<AuthorityResponse> getAuthoritiesByRole(Integer roleId) {
        return roleAuthorityRepository.findAuthoritiesByRoleId(roleId).stream()
                .map(this::mapToAuthorityResponse)
                .collect(Collectors.toSet());
    }

    /**
     * Get all authorities for a user (from roles + direct assignments)
     */
    @Transactional(readOnly = true)
    public Set<AuthorityResponse> getAllUserAuthorities(Long userId) {
        return userAuthorityRepository.findAllUserAuthorities(userId).stream()
                .map(this::mapToAuthorityResponse)
                .collect(Collectors.toSet());
    }

    /**
     * Check if user has specific authority
     */
    @Transactional(readOnly = true)
    public boolean userHasAuthority(Long userId, String authorityName) {
        Set<Authority> authorities = userAuthorityRepository.findAllUserAuthorities(userId);
        return authorities.stream()
                .anyMatch(authority -> authority.getName().equals(authorityName));
    }

    // ============= Helper Methods =============

    private Authority findAuthorityOrThrow(Integer authorityId) {
        return authorityRepository.findById(authorityId)
                .orElseThrow(() -> new ResourceNotFoundException("Authority not found: " + authorityId));
    }

    private void validateSuperUser(Long userId) {
        Set<Role> roles = userRoleRepository.findRolesByUserId(userId);
        boolean isSuperUser = roles.stream()
                .anyMatch(role -> "SUPER_USER".equals(role.getName()));

        if (!isSuperUser) {
            throw new ForbiddenException("Only SUPER_USER can perform this operation");
        }
    }

    private Set<Authority> validateAndFetchAuthorities(Set<String> authorityNames) {
        Set<Authority> authorities = new HashSet<>();
        for (String authorityName : authorityNames) {
            Authority authority = authorityRepository.findByName(authorityName)
                    .orElseThrow(() -> new ResourceNotFoundException("Authority not found: " + authorityName));
            authorities.add(authority);
        }
        return authorities;
    }

    private AuthorityResponse mapToAuthorityResponse(Authority authority) {
        return AuthorityResponse.builder()
                .id(authority.getId())
                .name(authority.getName())
                .description(authority.getDescription())
                .resource(authority.getResource())
                .action(authority.getAction())
                .createdAt(authority.getCreatedAt())
                .build();
    }
}