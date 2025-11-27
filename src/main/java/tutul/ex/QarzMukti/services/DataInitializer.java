package tutul.ex.QarzMukti.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import tutul.ex.QarzMukti.entities.*;
import tutul.ex.QarzMukti.repositories.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * Initialize default roles, authorities and super admin user on application
 * startup
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final RoleRepository roleRepository;
    private final AuthorityRepository authorityRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserDocumentsRepository userDocumentsRepository;
    private final RoleAuthorityRepository roleAuthorityRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initializeData() {
        return args -> {
            log.info("========================================");
            log.info("Starting data initialization...");
            log.info("========================================");

            // Step 1: Initialize authorities
            Map<String, Authority> authorities = initializeAuthorities();

            // Step 2: Initialize roles
            Map<String, Role> roles = initializeRoles();

            // Step 3: Assign authorities to roles
            assignAuthoritiesToRoles(roles, authorities);

            // Step 4: Initialize super admin user
            initializeSuperAdmin(roles);

            log.info("========================================");
            log.info("Data initialization completed successfully");
            log.info("========================================");
        };
    }

    private Map<String, Authority> initializeAuthorities() {
        log.info("Initializing authorities...");

        Map<String, Authority> authorityMap = new HashMap<>();

        List<AuthorityConfig> authorityConfigs = Arrays.asList(
                // User Management
                new AuthorityConfig("USER:CREATE", "Create new users", "USER", "CREATE"),
                new AuthorityConfig("USER:READ", "View user information", "USER", "READ"),
                new AuthorityConfig("USER:UPDATE", "Update user information", "USER", "UPDATE"),
                new AuthorityConfig("USER:DELETE", "Delete users", "USER", "DELETE"),
                new AuthorityConfig("USER:VERIFY", "Verify user accounts", "USER", "VERIFY"),
                new AuthorityConfig("USER:ASSIGN_ROLE", "Assign roles to users", "USER", "ASSIGN_ROLE"),

                // Role Management
                new AuthorityConfig("ROLE:CREATE", "Create new roles", "ROLE", "CREATE"),
                new AuthorityConfig("ROLE:READ", "View roles", "ROLE", "READ"),
                new AuthorityConfig("ROLE:UPDATE", "Update roles", "ROLE", "UPDATE"),
                new AuthorityConfig("ROLE:DELETE", "Delete roles", "ROLE", "DELETE"),

                // Authority Management
                new AuthorityConfig("AUTHORITY:CREATE", "Create new authorities", "AUTHORITY", "CREATE"),
                new AuthorityConfig("AUTHORITY:READ", "View authorities", "AUTHORITY", "READ"),
                new AuthorityConfig("AUTHORITY:UPDATE", "Update authorities", "AUTHORITY", "UPDATE"),
                new AuthorityConfig("AUTHORITY:DELETE", "Delete authorities", "AUTHORITY", "DELETE"),
                new AuthorityConfig("AUTHORITY:ASSIGN", "Assign authorities to roles", "AUTHORITY", "ASSIGN"),

                // Loan Management
                new AuthorityConfig("LOAN:CREATE", "Create loan applications", "LOAN", "CREATE"),
                new AuthorityConfig("LOAN:READ", "View loan details", "LOAN", "READ"),
                new AuthorityConfig("LOAN:UPDATE", "Update loan information", "LOAN", "UPDATE"),
                new AuthorityConfig("LOAN:DELETE", "Delete loans", "LOAN", "DELETE"),
                new AuthorityConfig("LOAN:APPROVE", "Approve loans", "LOAN", "APPROVE"),
                new AuthorityConfig("LOAN:REJECT", "Reject loans", "LOAN", "REJECT"),
                new AuthorityConfig("LOAN:DISBURSE", "Disburse loan funds", "LOAN", "DISBURSE"),

                // Fund Management
                new AuthorityConfig("FUND:CREATE", "Create treasury funds", "FUND", "CREATE"),
                new AuthorityConfig("FUND:READ", "View fund balances", "FUND", "READ"),
                new AuthorityConfig("FUND:ALLOCATE", "Allocate funds to accountants", "FUND", "ALLOCATE"),
                new AuthorityConfig("FUND:ADJUST", "Make fund adjustments", "FUND", "ADJUST"),

                // Interest Management
                new AuthorityConfig("INTEREST:READ", "View interest rates", "INTEREST", "READ"),
                new AuthorityConfig("INTEREST:UPDATE", "Update system interest rates", "INTEREST", "UPDATE"),
                new AuthorityConfig("INTEREST:OVERRIDE", "Override interest rates for loans", "INTEREST", "OVERRIDE"),

                // Payment Management
                new AuthorityConfig("PAYMENT:CREATE", "Initiate payments", "PAYMENT", "CREATE"),
                new AuthorityConfig("PAYMENT:READ", "View payment transactions", "PAYMENT", "READ"),
                new AuthorityConfig("PAYMENT:RECONCILE", "Reconcile payments", "PAYMENT", "RECONCILE"),

                // Reporting
                new AuthorityConfig("REPORT:VIEW", "View reports", "REPORT", "VIEW"),
                new AuthorityConfig("REPORT:EXPORT", "Export reports", "REPORT", "EXPORT"),

                // Audit
                new AuthorityConfig("AUDIT:READ", "View audit logs", "AUDIT", "READ"));

        for (AuthorityConfig config : authorityConfigs) {
            if (!authorityRepository.existsByName(config.name)) {
                Authority authority = Authority.builder()
                        .name(config.name)
                        .description(config.description)
                        .resource(config.resource)
                        .action(config.action)
                        .createdAt(OffsetDateTime.now())
                        .build();
                authority = authorityRepository.save(authority);
                authorityMap.put(config.name, authority);
                log.info("Created authority: {}", config.name);
            } else {
                Authority authority = authorityRepository.findByName(config.name).get();
                authorityMap.put(config.name, authority);
                log.debug("Authority already exists: {}", config.name);
            }
        }

        log.info("Authorities initialization completed. Total: {}", authorityMap.size());
        return authorityMap;
    }

    private Map<String, Role> initializeRoles() {
        log.info("Initializing roles...");

        Map<String, Role> roleMap = new HashMap<>();

        List<RoleConfig> roleConfigs = Arrays.asList(
                new RoleConfig("SUPER_USER", "Super User with full system access and authority management"),
                new RoleConfig("ADMIN", "Administrator with system management capabilities"),
                new RoleConfig("ACCOUNTANT", "Accountant who approves loans and manages funds"),
                new RoleConfig("USER", "Regular user who can apply for loans and make payments"));

        for (RoleConfig config : roleConfigs) {
            if (!roleRepository.existsByName(config.name)) {
                Role role = Role.builder()
                        .name(config.name)
                        .description(config.description)
                        .createdAt(OffsetDateTime.now())
                        .build();
                role = roleRepository.save(role);
                roleMap.put(config.name, role);
                log.info("Created role: {}", config.name);
            } else {
                Role role = roleRepository.findByName(config.name).get();
                roleMap.put(config.name, role);
                log.debug("Role already exists: {}", config.name);
            }
        }

        log.info("Roles initialization completed. Total: {}", roleMap.size());
        return roleMap;
    }

    private void assignAuthoritiesToRoles(Map<String, Role> roles, Map<String, Authority> authorities) {
        log.info("Assigning authorities to roles...");

        // SUPER_USER gets ALL authorities
        Role superUserRole = roles.get("SUPER_USER");
        if (superUserRole != null) {
            for (Authority authority : authorities.values()) {
                assignAuthorityToRole(superUserRole.getId(), authority.getId());
            }
            log.info("Assigned ALL authorities to SUPER_USER role");
        }

        // ADMIN gets most authorities except authority management
        Role adminRole = roles.get("ADMIN");
        if (adminRole != null) {
            List<String> adminAuthorities = Arrays.asList(
                    "USER:CREATE", "USER:READ", "USER:UPDATE", "USER:DELETE", "USER:VERIFY", "USER:ASSIGN_ROLE",
                    "ROLE:READ",
                    "LOAN:READ", "LOAN:UPDATE", "LOAN:DELETE",
                    "FUND:READ", "FUND:ADJUST",
                    "INTEREST:READ",
                    "PAYMENT:READ", "PAYMENT:RECONCILE",
                    "REPORT:VIEW", "REPORT:EXPORT",
                    "AUDIT:READ");

            for (String authName : adminAuthorities) {
                Authority authority = authorities.get(authName);
                if (authority != null) {
                    assignAuthorityToRole(adminRole.getId(), authority.getId());
                }
            }
            log.info("Assigned {} authorities to ADMIN role", adminAuthorities.size());
        }

        // ACCOUNTANT gets loan approval and fund management
        Role accountantRole = roles.get("ACCOUNTANT");
        if (accountantRole != null) {
            List<String> accountantAuthorities = Arrays.asList(
                    "USER:READ",
                    "LOAN:READ", "LOAN:APPROVE", "LOAN:REJECT", "LOAN:DISBURSE",
                    "FUND:READ",
                    "INTEREST:READ", "INTEREST:OVERRIDE",
                    "PAYMENT:READ",
                    "REPORT:VIEW");

            for (String authName : accountantAuthorities) {
                Authority authority = authorities.get(authName);
                if (authority != null) {
                    assignAuthorityToRole(accountantRole.getId(), authority.getId());
                }
            }
            log.info("Assigned {} authorities to ACCOUNTANT role", accountantAuthorities.size());
        }

        // USER gets basic operations
        Role userRole = roles.get("USER");
        if (userRole != null) {
            List<String> userAuthorities = Arrays.asList(
                    "LOAN:CREATE", "LOAN:READ",
                    "PAYMENT:CREATE", "PAYMENT:READ");

            for (String authName : userAuthorities) {
                Authority authority = authorities.get(authName);
                if (authority != null) {
                    assignAuthorityToRole(userRole.getId(), authority.getId());
                }
            }
            log.info("Assigned {} authorities to USER role", userAuthorities.size());
        }

        log.info("Authority assignment to roles completed");
    }

    private void assignAuthorityToRole(Integer roleId, Integer authorityId) {
        if (!roleAuthorityRepository.existsByRoleIdAndAuthorityId(roleId, authorityId)) {
            RoleAuthority roleAuthority = RoleAuthority.builder()
                    .roleId(roleId)
                    .authorityId(authorityId)
                    .assignedAt(OffsetDateTime.now())
                    .build();
            roleAuthorityRepository.save(roleAuthority);
        }
    }

    private void initializeSuperAdmin(Map<String, Role> roles) {
        var superAdminUsername = "superadmin";

        User superAdmin = userRepository.findByUsername(superAdminUsername)
                .orElse(null);

        if (superAdmin == null) {
            superAdmin = User.builder()
                    .username(superAdminUsername)
                    .passwordHash(passwordEncoder.encode("Password@101"))
                    .fullName("Super Administrator")
                    .email("admin@qarzmukti.dev")
                    .phone("01700000000")
                    .nationalId("0000000000")
                    .dateOfBirth(LocalDate.of(1990, 1, 1))
                    .address("System")
                    .city("System")
                    .postalCode("0000")
                    .enabled(true)
                    .verified(true)
                    .verificationStatus("APPROVED")
                    .createdAt(OffsetDateTime.now())
                    .build();

            superAdmin = userRepository.save(superAdmin);

            UserDocuments documents = UserDocuments.builder()
                    .id(UUID.randomUUID())
                    .userId((superAdmin.getId()))
                    .createdAt(OffsetDateTime.now())
                    .build();

            userDocumentsRepository.save(documents);
        } else {
            // Update existing super admin to ensure credentials match
            boolean changed = false;
            if (!"admin@qarzmukti.dev".equals(superAdmin.getEmail())) {
                superAdmin.setEmail("admin@qarzmukti.dev");
                changed = true;
            }
            // We can also enforce password update if needed, but we can't easily check
            // encoded password equality without matching
            // For now, let's just ensure email is correct as that's the login issue.

            if (changed) {
                userRepository.save(superAdmin);
                log.info("Updated Super Admin email");
            }
        }

        // No stale entity; this one is managed.
        Role superUserRole = roles.get("SUPER_USER");
        if (superUserRole != null) {
            assignRoleToUser(Long.valueOf(superAdmin.getId().toString()), superUserRole.getId());
        }

        log.info("Super Admin initialized");
    }

    private void assignRoleToUser(Long userId, Integer roleId) {
        if (!userRoleRepository.existsByUserIdAndRoleId(userId, roleId)) {
            UserRole userRole = UserRole.builder()
                    .userId(userId)
                    .roleId(roleId)
                    .assignedAt(OffsetDateTime.now())
                    .build();
            userRoleRepository.save(userRole);
        }
    }

    // Helper classes for configuration
    @lombok.AllArgsConstructor
    private static class AuthorityConfig {
        String name;
        String description;
        String resource;
        String action;
    }

    @lombok.AllArgsConstructor
    private static class RoleConfig {
        String name;
        String description;
    }
}