package tutul.ex.QarzMukti.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tutul.ex.QarzMukti.enums.OnboardingStatus;
import org.springframework.web.multipart.MultipartFile;
import tutul.ex.QarzMukti.common.DuplicateResourceException;
import tutul.ex.QarzMukti.common.ForbiddenException;
import tutul.ex.QarzMukti.common.ResourceNotFoundException;
import tutul.ex.QarzMukti.common.ValidationException;
import tutul.ex.QarzMukti.dto.*;
import tutul.ex.QarzMukti.entities.*;
import tutul.ex.QarzMukti.repositories.*;
import tutul.ex.QarzMukti.security.CustomUserDetails;
import tutul.ex.QarzMukti.specifications.UserSpecification;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserDocumentsRepository userDocumentsRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final AuthorityRepository authorityRepository;
    private final UserAuthorityRepository userAuthorityRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    @Transactional
    public UserResponse registerUser(UserRegistrationRequest request) {
        log.info("Registering new user: {}", request.getUsername());

        // Validate uniqueness
        validateUserUniqueness(request);

        // Create user entity with PENDING status
        User user = User.builder()
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .nationalId(request.getNationalId())
                .dateOfBirth(request.getDateOfBirth())
                .address(request.getAddress())
                .city(request.getCity())
                .postalCode(request.getPostalCode())
                .enabled(true)
                .verified(false)
                .verificationStatus("PENDING")
                .onboardingStatus(OnboardingStatus.REGISTERED)
                .createdAt(OffsetDateTime.now())
                .build();

        user = userRepository.save(user);

        // Create user documents entry
        UserDocuments documents = UserDocuments.builder()
                .id(UUID.randomUUID())
                .userId(user.getId())
                .nationalIdDocumentPath(request.getNationalIdDocumentPath())
                .electricityBillDocumentPath(request.getElectricityBillDocumentPath())
                .profilePhotoPath(request.getProfilePhotoPath())
                .createdAt(OffsetDateTime.now())
                .build();

        userDocumentsRepository.save(documents);

        log.info("User registered successfully with PENDING status: {}", user.getId());
        return mapToUserResponse(user, new HashSet<>());
    }

    /**
     * Upload user documents
     */
    @Transactional
    public DocumentUploadResponse uploadDocument(
            String userId,
            String documentType,
            MultipartFile file) {

        log.info("Uploading {} for user: {}", documentType, userId);

        User user = findUserOrThrow((userId));

        // Validate file
        fileStorageService.validateFile(file, documentType);

        // Store file
        String filePath = fileStorageService.store(file, userId, documentType);

        // Update documents
        UserDocuments documents = userDocumentsRepository.findByUserId(Long.valueOf(userId))
                .orElse(UserDocuments.builder()
                        .id(UUID.randomUUID())
                        .userId(Long.valueOf(userId))
                        .createdAt(OffsetDateTime.now())
                        .build());

        OffsetDateTime now = OffsetDateTime.now();

        switch (documentType.toUpperCase()) {
            case "NATIONAL_ID":
                documents.setNationalIdDocumentPath(filePath);
                documents.setNationalIdUploadedAt(now);
                break;
            case "ELECTRICITY_BILL":
                documents.setElectricityBillDocumentPath(filePath);
                documents.setElectricityBillUploadedAt(now);
                break;
            case "PROFILE_PHOTO":
                documents.setProfilePhotoPath(filePath);
                documents.setProfilePhotoUploadedAt(now);
                break;
            default:
                throw new ValidationException("Invalid document type: " + documentType);
        }

        documents.setUpdatedAt(now);
        userDocumentsRepository.save(documents);

        // Update user's onboarding status based on document completion
        updateOnboardingStatusBasedOnDocuments(user, documents);

        log.info("Document uploaded successfully: {} for user: {}", documentType, userId);

        return DocumentUploadResponse.builder()
                .documentType(documentType)
                .fileName(file.getOriginalFilename())
                .filePath(filePath)
                .fileSize(file.getSize())
                .uploadedAt(now)
                .build();
    }

    /**
     * Verify/Approve user (requires USER:VERIFY authority)
     * Can assign roles during approval
     */
    @Transactional
    public UserResponse verifyUser(UserVerificationRequest request) {
        Long authenticatedUserId = getCurrentAuthenticatedUserId();
        log.info("Verifying user: {} by authenticated user: {}", request.getUserId(), authenticatedUserId);

        User user = findUserOrThrow(String.valueOf(request.getUserId()));

        if ("APPROVED".equals(request.getStatus())) {
            user.setVerified(true);
            user.setVerificationStatus("APPROVED");
            user.setVerifiedBy(authenticatedUserId);
            user.setVerifiedAt(OffsetDateTime.now());
            user.setVerificationComments(request.getComments());

            // Assign roles if provided
            if (request.getAssignedRoleNames() != null && !request.getAssignedRoleNames().isEmpty()) {
                Set<Role> roles = validateAndFetchRoles(request.getAssignedRoleNames());
                assignRolesToUser(user, roles, authenticatedUserId);
            }

        } else if ("REJECTED".equals(request.getStatus())) {
            user.setVerified(false);
            user.setVerificationStatus("REJECTED");
            user.setVerifiedBy(authenticatedUserId);
            user.setVerifiedAt(OffsetDateTime.now());
            user.setVerificationComments(request.getComments());
            user.setEnabled(false); // Disable rejected users
        }

        user.setUpdatedAt(OffsetDateTime.now());
        user = userRepository.save(user);

        Set<Authority> authorities = userAuthorityRepository.findAllUserAuthorities(user.getId());
        log.info("User verification completed: {} with status: {}", user.getId(), request.getStatus());
        return mapToUserResponse(user,authorities);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        User user = findUserOrThrow(String.valueOf((userId)));
        Set<Authority> authorities = userAuthorityRepository.findAllUserAuthorities(userId);
        return mapToUserResponse(user, authorities);
    }

    /**
     * Update user information
     */
    @Transactional
    public UserResponse updateUser(String userId, UserUpdateRequest request) {
        log.info("Updating user: {}", userId);

        User user = findUserOrThrow(userId);

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateResourceException("Email already exists: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
        }

        if (request.getFullName() != null)
            user.setFullName(request.getFullName());
        if (request.getPhone() != null)
            user.setPhone(request.getPhone());
        if (request.getAddress() != null)
            user.setAddress(request.getAddress());
        if (request.getCity() != null)
            user.setCity(request.getCity());
        if (request.getPostalCode() != null)
            user.setPostalCode(request.getPostalCode());
        if (request.getEnabled() != null)
            user.setEnabled(request.getEnabled());

        user.setUpdatedAt(OffsetDateTime.now());
        user = userRepository.save(user);

        Set<Authority> authorities = userAuthorityRepository.findAllUserAuthorities(Long.valueOf(userId));

        log.info("User updated successfully: {}", userId);
        return mapToUserResponse(user,authorities);
    }

    /**
     * Change user password
     */
    @Transactional
    public void changePassword(String userId, UserPasswordChangeRequest request) {
        log.info("Changing password for user: {}", userId);

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ValidationException("New password and confirmation do not match");
        }

        User user = findUserOrThrow(userId);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new ValidationException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(OffsetDateTime.now());
        userRepository.save(user);

        log.info("Password changed successfully for user: {}", userId);
    }

    /**
     * Assign roles to user (requires USER:ASSIGN_ROLE authority, replaces existing
     * roles)
     */
    @Transactional
    public UserResponse assignRolesBySuperUser(Long userId, Set<String> roleNames) {
        Long authenticatedUserId = getCurrentAuthenticatedUserId();
        log.info("Assigning roles to user: {} by authenticated user: {}", userId, authenticatedUserId);

        User user = findUserOrThrow(String.valueOf(userId));
        Set<Role> roles = validateAndFetchRoles(roleNames);

        // Remove existing roles
        userRoleRepository.deleteByUserId(userId);

        // Assign new roles
        assignRolesToUser(user, roles, authenticatedUserId);

        Set<Authority> authorities = userAuthorityRepository.findAllUserAuthorities(userId);

        log.info("Roles assigned successfully to user: {}", userId);
        return mapToUserResponse(user, authorities);
    }

    /**
     * Assign authorities directly to user (requires USER:ASSIGN_ROLE authority)
     */
    @Transactional
    public UserResponse assignAuthoritiesToUser(UserAuthorityAssignmentRequest request) {
        Long authenticatedUserId = getCurrentAuthenticatedUserId();
        log.info("Assigning authorities to user: {} by authenticated user: {}", request.getUserId(),
                authenticatedUserId);

        User user = findUserOrThrow(String.valueOf(request.getUserId()));
        Set<Authority> authorities = validateAndFetchAuthorities(request.getAuthorityNames());

        // Remove existing direct authorities
        userAuthorityRepository.deleteByUserId(request.getUserId());

        // Assign new authorities
        for (Authority authority : authorities) {
            UserAuthority userAuthority = UserAuthority.builder()
                    .userId(user.getId())
                    .authorityId(authority.getId())
                    .assignedBy(authenticatedUserId)
                    .assignedAt(OffsetDateTime.now())
                    .build();
            userAuthorityRepository.save(userAuthority);
        }

        Set<Authority> allAuthorities = userAuthorityRepository.findAllUserAuthorities(user.getId());

        log.info("Authorities assigned successfully to user: {}", request.getUserId());
        return mapToUserResponse(user,allAuthorities);
    }

    /**
     * Search users with criteria
     */
    @Transactional(readOnly = true)
    public Page<UserListResponse> searchUsers(UserSearchCriteria criteria) {
        log.info("Searching users with criteria: {}", criteria);

        Pageable pageable = PageRequest.of(
                criteria.getPage(),
                criteria.getSize(),
                Sort.Direction.fromString(criteria.getSortDirection()),
                criteria.getSortBy());

        Page<User> usersPage = userRepository.findAll(
                UserSpecification.withFilters(criteria),
                pageable);

        return usersPage.map(user -> {
            Set<Role> roles = userRoleRepository.findRolesByUserId((user.getId()));
            return mapToUserListResponse(user, roles);
        });
    }

    /**
     * Get all pending verification users
     */
    @Transactional(readOnly = true)
    public List<UserListResponse> getPendingVerificationUsers() {
        List<User> users = userRepository.findByVerificationStatus("PENDING");
        return users.stream()
                .map(user -> {
                    Set<Role> roles = userRoleRepository.findRolesByUserId((user.getId()));
                    return mapToUserListResponse(user, roles);
                })
                .collect(Collectors.toList());
    }

    /**
     * Get detailed onboarding status for a user
     */
    @Transactional(readOnly = true)
    public OnboardingStatusResponse getOnboardingStatus(Long userId) {
        log.info("Getting onboarding status for user: {}", userId);
        
        User user = findUserOrThrow(String.valueOf(userId));
        UserDocuments documents = userDocumentsRepository.findByUserId(userId).orElse(null);
        
        // Calculate document completion status
        OnboardingStatusResponse.DocumentCompletionStatus docStatus = calculateDocumentCompletionStatus(documents);
        
        // Check loan eligibility
        boolean isEligible = checkLoanEligibility(userId);
        String eligibilityMessage = buildEligibilityMessage(user, isEligible);
        
        return OnboardingStatusResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .onboardingStatus(user.getOnboardingStatus())
                .documentStatus(docStatus)
                .isEligibleForLoan(isEligible)
                .eligibilityMessage(eligibilityMessage)
                .onboardingCompletedAt(user.getOnboardingCompletedAt())
                .build();
    }

    /**
     * Complete user onboarding (can be called explicitly or happens automatically)
     */
    @Transactional
    public UserResponse completeOnboarding(Long userId) {
        log.info("Completing onboarding for user: {}", userId);
        
        User user = findUserOrThrow(String.valueOf(userId));
        UserDocuments documents = userDocumentsRepository.findByUserId(userId)
                .orElseThrow(() -> new ValidationException("No documents found for user"));
        
        // Validate all documents are uploaded
        if (!areAllDocumentsUploaded(documents)) {
            throw new ValidationException("Cannot complete onboarding. All required documents must be uploaded.");
        }
        
        // Update onboarding status
        user.setOnboardingStatus(OnboardingStatus.ONBOARDING_COMPLETED);
        user.setOnboardingCompletedAt(OffsetDateTime.now());
        user.setUpdatedAt(OffsetDateTime.now());
        user = userRepository.save(user);
        
        Set<Authority> authorities = userAuthorityRepository.findAllUserAuthorities(userId);
        
        log.info("Onboarding completed for user: {}", userId);
        return mapToUserResponse(user,authorities);
    }

    /**
     * Check if user is eligible to apply for loans
     */
    @Transactional(readOnly = true)
    public Boolean checkLoanEligibility(Long userId) {
        User user = findUserOrThrow(String.valueOf(userId));
        
        return user.getOnboardingStatus() == OnboardingStatus.ONBOARDING_COMPLETED
                && "APPROVED".equals(user.getVerificationStatus())
                && user.getVerified()
                && user.getEnabled();
    }

    /**
     * Get detailed loan eligibility information
     */
    @Transactional(readOnly = true)
    public LoanEligibilityResponse getLoanEligibility(Long userId) {
        log.info("Checking loan eligibility for user: {}", userId);
        
        User user = findUserOrThrow(String.valueOf(userId));
        UserDocuments documents = userDocumentsRepository.findByUserId(userId).orElse(null);
        
        boolean onboardingCompleted = user.getOnboardingStatus() == OnboardingStatus.ONBOARDING_COMPLETED;
        boolean documentsUploaded = areAllDocumentsUploaded(documents);
        boolean adminVerified = "APPROVED".equals(user.getVerificationStatus()) && user.getVerified();
        boolean accountEnabled = user.getEnabled();
        
        boolean isEligible = onboardingCompleted && adminVerified && accountEnabled;
        
        String message = buildEligibilityMessage(user, isEligible);
        
        LoanEligibilityResponse.EligibilityDetails details = LoanEligibilityResponse.EligibilityDetails.builder()
                .onboardingCompleted(onboardingCompleted)
                .documentsUploaded(documentsUploaded)
                .adminVerified(adminVerified)
                .accountEnabled(accountEnabled)
                .currentOnboardingStatus(user.getOnboardingStatus().name())
                .currentVerificationStatus(user.getVerificationStatus())
                .build();
        
        return LoanEligibilityResponse.builder()
                .userId(user.getId())
                .isEligible(isEligible)
                .message(message)
                .details(details)
                .build();
    }

    // ============= Helper Methods =============

    /**
     * Get the current authenticated user's ID from Spring Security context
     */
    public Long getCurrentAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ForbiddenException("No authenticated user found");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getUser().getId();
        }

        throw new ForbiddenException("Invalid authentication principal");
    }

    private User findUserOrThrow(String userId) {
        return userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    private void validateUserUniqueness(UserRegistrationRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already exists: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + request.getEmail());
        }
        if (userRepository.existsByNationalId(request.getNationalId())) {
            throw new DuplicateResourceException("National ID already exists: " + request.getNationalId());
        }
    }

    private Set<Role> validateAndFetchRoles(Set<String> roleNames) {
        Set<Role> roles = new HashSet<>();
        for (String roleName : roleNames) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
            roles.add(role);
        }
        return roles;
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

    private void assignRolesToUser(User user, Set<Role> roles, Long assignedBy) {
        for (Role role : roles) {
            if (!userRoleRepository.existsByUserIdAndRoleId(user.getId(), role.getId())) {
                UserRole userRole = UserRole.builder()
                        .userId((user.getId()))
                        .roleId(role.getId())
                        .assignedBy(assignedBy)
                        .assignedAt(OffsetDateTime.now())
                        .build();
                userRoleRepository.save(userRole);
            }
        }
    }

    @Transactional
    protected UserResponse mapToUserResponse(User user, Set<Authority> authorities) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .dateOfBirth(user.getDateOfBirth())
                .enabled(user.getEnabled())
                .verificationStatus(user.getVerificationStatus())
                .onboardingStatus(user.getOnboardingStatus())
                .onboardingCompletedAt(user.getOnboardingCompletedAt())
                .isEligibleForLoan(checkLoanEligibility(user.getId()))
                .authorities(authorities.stream()
                        .map(authority -> AuthorityResponse.builder()
                                .id(authority.getId())
                                .name(authority.getName())
                                .description(authority.getDescription())
                                .resource(authority.getResource())
                                .action(authority.getAction())
                                .createdAt(authority.getCreatedAt())
                                .build())
                        .collect(Collectors.toSet()))

                .createdAt(user.getCreatedAt())
                .build();
    }

    private UserListResponse mapToUserListResponse(User user, Set<Role> roles) {
        return UserListResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .enabled(user.getEnabled())
                .verified(user.getVerified())
                .verificationStatus(user.getVerificationStatus())
                .onboardingStatus(user.getOnboardingStatus())
                .build();
    }

    // ============= Onboarding Status Helper Methods =============

    /**
     * Update user's onboarding status based on document completion
     */
    private void updateOnboardingStatusBasedOnDocuments(User user, UserDocuments documents) {
        OnboardingStatus currentStatus = user.getOnboardingStatus();
        OnboardingStatus newStatus = currentStatus;
        
        boolean allDocsUploaded = areAllDocumentsUploaded(documents);
        boolean anyDocUploaded = isAnyDocumentUploaded(documents);
        
        // Determine new status based on document completion
        if (allDocsUploaded) {
            // All documents uploaded - automatically complete onboarding
            newStatus = OnboardingStatus.ONBOARDING_COMPLETED;
            if (user.getOnboardingCompletedAt() == null) {
                user.setOnboardingCompletedAt(OffsetDateTime.now());
            }
        } else if (anyDocUploaded && currentStatus == OnboardingStatus.REGISTERED) {
            // At least one document uploaded - move to pending
            newStatus = OnboardingStatus.DOCUMENTS_PENDING;
        }
        
        // Update status if changed
        if (newStatus != currentStatus) {
            user.setOnboardingStatus(newStatus);
            user.setUpdatedAt(OffsetDateTime.now());
            userRepository.save(user);
            log.info("Updated onboarding status for user {} from {} to {}", 
                    user.getId(), currentStatus, newStatus);
        }
    }

    /**
     * Check if all required documents are uploaded
     */
    private boolean areAllDocumentsUploaded(UserDocuments documents) {
        if (documents == null) {
            return false;
        }
        return documents.getNationalIdDocumentPath() != null 
                && documents.getElectricityBillDocumentPath() != null 
                && documents.getProfilePhotoPath() != null;
    }

    /**
     * Check if any document is uploaded
     */
    private boolean isAnyDocumentUploaded(UserDocuments documents) {
        if (documents == null) {
            return false;
        }
        return documents.getNationalIdDocumentPath() != null 
                || documents.getElectricityBillDocumentPath() != null 
                || documents.getProfilePhotoPath() != null;
    }

    /**
     * Calculate document completion status
     */
    private OnboardingStatusResponse.DocumentCompletionStatus calculateDocumentCompletionStatus(
            UserDocuments documents) {
        
        boolean nationalIdUploaded = documents != null && documents.getNationalIdDocumentPath() != null;
        boolean electricityBillUploaded = documents != null && documents.getElectricityBillDocumentPath() != null;
        boolean profilePhotoUploaded = documents != null && documents.getProfilePhotoPath() != null;
        
        int totalRequired = 3;
        int totalUploaded = (nationalIdUploaded ? 1 : 0) 
                + (electricityBillUploaded ? 1 : 0) 
                + (profilePhotoUploaded ? 1 : 0);
        
        double completionPercentage = (totalUploaded * 100.0) / totalRequired;
        
        return OnboardingStatusResponse.DocumentCompletionStatus.builder()
                .nationalIdUploaded(nationalIdUploaded)
                .electricityBillUploaded(electricityBillUploaded)
                .profilePhotoUploaded(profilePhotoUploaded)
                .totalRequired(totalRequired)
                .totalUploaded(totalUploaded)
                .completionPercentage(completionPercentage)
                .build();
    }

    /**
     * Build eligibility message based on user status
     */
    private String buildEligibilityMessage(User user, boolean isEligible) {
        if (isEligible) {
            return "You are eligible to apply for loans.";
        }
        
        // Provide specific reason for ineligibility
        if (user.getOnboardingStatus() != OnboardingStatus.ONBOARDING_COMPLETED) {
            return "Please complete your onboarding by uploading all required documents.";
        }
        
        if (!"APPROVED".equals(user.getVerificationStatus()) || !user.getVerified()) {
            return "Your account is pending admin verification. Please wait for approval.";
        }
        
        if (!user.getEnabled()) {
            return "Your account is disabled. Please contact support.";
        }
        
        return "You are not eligible to apply for loans at this time.";
    }
}