package tutul.ex.QarzMukti.features.rbac;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tutul.ex.QarzMukti.shared.constants.ApiEndpoints;
import tutul.ex.QarzMukti.shared.dto.ApiResponse;
import tutul.ex.QarzMukti.shared.dto.AuthorityRequest;
import tutul.ex.QarzMukti.shared.dto.AuthorityResponse;
import tutul.ex.QarzMukti.shared.dto.RoleAuthorityAssignmentRequest;
import tutul.ex.QarzMukti.features.rbac.AuthorityService;

import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping(ApiEndpoints.Authorities.BASE)
@RequiredArgsConstructor
@Validated
public class AuthorityController {

    private final AuthorityService authorityService;

    @PostMapping(ApiEndpoints.Authorities.BASE)
    public ResponseEntity<ApiResponse<AuthorityResponse>> createAuthority(
            @RequestHeader("X-User-Id") Long superUserId,
            @Valid @RequestBody AuthorityRequest request) {
        log.info("Creating new authority - superUserId: {}, authorityName: {}", superUserId, request.getName());
        AuthorityResponse response = authorityService.createAuthority(superUserId, request);
        log.info("Authority created successfully - authorityId: {}, name: {}", response.getId(), response.getName());
        return new ResponseEntity<>(ApiResponse.success(response, "Authority created successfully"),
                HttpStatus.CREATED);
    }

    @GetMapping(ApiEndpoints.Authorities.BASE)
    public ResponseEntity<ApiResponse<List<AuthorityResponse>>> getAllAuthorities() {
        log.info("Fetching all authorities");
        List<AuthorityResponse> response = authorityService.getAllAuthorities();
        log.info("Found {} authorities", response.size());
        return new ResponseEntity<>(ApiResponse.success(response, "Authorities retrieved successfully"), HttpStatus.OK);
    }

    @GetMapping(ApiEndpoints.Authorities.BY_ID)
    public ResponseEntity<ApiResponse<AuthorityResponse>> getAuthorityById(@PathVariable Integer authorityId) {
        log.info("Fetching authority by authorityId: {}", authorityId);
        AuthorityResponse response = authorityService.getAuthorityById(authorityId);
        return new ResponseEntity<>(ApiResponse.success(response, "Authority retrieved successfully"), HttpStatus.OK);
    }

    @GetMapping(ApiEndpoints.Authorities.BY_NAME)
    public ResponseEntity<ApiResponse<AuthorityResponse>> getAuthorityByName(@PathVariable String name) {
        log.info("Fetching authority by name: {}", name);
        AuthorityResponse response = authorityService.getAuthorityByName(name);
        return new ResponseEntity<>(ApiResponse.success(response, "Authority retrieved successfully"), HttpStatus.OK);
    }

    @GetMapping(ApiEndpoints.Authorities.BY_RESOURCE)
    public ResponseEntity<ApiResponse<List<AuthorityResponse>>> getAuthoritiesByResource(
            @PathVariable String resource) {
        log.info("Fetching authorities for resource: {}", resource);
        List<AuthorityResponse> response = authorityService.getAuthoritiesByResource(resource);
        log.info("Found {} authorities for resource: {}", response.size(), resource);
        return new ResponseEntity<>(ApiResponse.success(response, "Authorities for resource retrieved successfully"),
                HttpStatus.OK);
    }

    @PutMapping(ApiEndpoints.Authorities.BY_ID)
    public ResponseEntity<ApiResponse<AuthorityResponse>> updateAuthority(
            @RequestHeader("X-User-Id") Long superUserId,
            @PathVariable Integer authorityId,
            @Valid @RequestBody AuthorityRequest request) {
        log.info("Updating authority - superUserId: {}, authorityId: {}, newName: {}", superUserId, authorityId,
                request.getName());
        AuthorityResponse response = authorityService.updateAuthority(superUserId, authorityId, request);
        log.info("Authority updated successfully - authorityId: {}", authorityId);
        return new ResponseEntity<>(ApiResponse.success(response, "Authority updated successfully"), HttpStatus.OK);
    }

    @DeleteMapping(ApiEndpoints.Authorities.BY_ID)
    public ResponseEntity<ApiResponse<Void>> deleteAuthority(
            @RequestHeader("X-User-Id") Long superUserId,
            @PathVariable Integer authorityId) {
        log.info("Deleting authority - superUserId: {}, authorityId: {}", superUserId, authorityId);
        authorityService.deleteAuthority(superUserId, authorityId);
        log.info("Authority deleted successfully - authorityId: {}", authorityId);
        return new ResponseEntity<>(ApiResponse.success(null, "Authority deleted successfully"), HttpStatus.OK);
    }

    @PutMapping(ApiEndpoints.Authorities.ASSIGN_TO_ROLE)
    public ResponseEntity<ApiResponse<Void>> assignAuthoritiesToRole(
            @RequestHeader("X-User-Id") Long superUserId,
            @Valid @RequestBody RoleAuthorityAssignmentRequest request) {
        log.info("Assigning authorities to role - superUserId: {}, roleId: {}", superUserId, request.getRoleId());
        authorityService.assignAuthoritiesToRole(superUserId, request);
        log.info("Authorities assigned successfully to roleId: {}", request.getRoleId());
        return new ResponseEntity<>(ApiResponse.success(null, "Authorities assigned to role successfully"),
                HttpStatus.OK);
    }

    @GetMapping(ApiEndpoints.Authorities.BY_ROLE)
    public ResponseEntity<ApiResponse<Set<AuthorityResponse>>> getAuthoritiesByRole(@PathVariable Integer roleId) {
        log.info("Fetching authorities for roleId: {}", roleId);
        Set<AuthorityResponse> response = authorityService.getAuthoritiesByRole(roleId);
        log.info("Found {} authorities for roleId: {}", response.size(), roleId);
        return new ResponseEntity<>(ApiResponse.success(response, "Authorities for role retrieved successfully"),
                HttpStatus.OK);
    }

    @GetMapping(ApiEndpoints.Authorities.BY_USER)
    public ResponseEntity<ApiResponse<Set<AuthorityResponse>>> getAllUserAuthorities(@PathVariable Long userId) {
        log.info("Fetching all authorities for userId: {}", userId);
        Set<AuthorityResponse> response = authorityService.getAllUserAuthorities(userId);
        log.info("Found {} authorities for userId: {}", response.size(), userId);
        return new ResponseEntity<>(ApiResponse.success(response, "User authorities retrieved successfully"),
                HttpStatus.OK);
    }

    @GetMapping(ApiEndpoints.Authorities.USER_HAS_AUTHORITY)
    public ResponseEntity<ApiResponse<Boolean>> userHasAuthority(
            @PathVariable Long userId,
            @PathVariable String authorityName) {
        log.info("Checking if userId: {} has authority: {}", userId, authorityName);
        boolean hasAuthority = authorityService.userHasAuthority(userId, authorityName);
        log.info("User {} authority {} check result: {}", userId, authorityName, hasAuthority);
        return new ResponseEntity<>(
                ApiResponse.success(hasAuthority, hasAuthority ? "User has authority" : "User does not have authority"),
                HttpStatus.OK);
    }
}