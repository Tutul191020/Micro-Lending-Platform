package tutul.ex.QarzMukti.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tutul.ex.QarzMukti.dto.ApiResponse;
import tutul.ex.QarzMukti.dto.AuthorityRequest;
import tutul.ex.QarzMukti.dto.AuthorityResponse;
import tutul.ex.QarzMukti.dto.RoleAuthorityAssignmentRequest;
import tutul.ex.QarzMukti.services.AuthorityService;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/authorities")
@RequiredArgsConstructor
@Validated
public class AuthorityController {

    private final AuthorityService authorityService;

    /**
     * Create a new authority
     * POST /api/authorities
     * Requires: SUPER_USER role
     */
    @PostMapping
    public ResponseEntity<ApiResponse<AuthorityResponse>> createAuthority(
            @RequestHeader("X-User-Id") Long superUserId,
            @Valid @RequestBody AuthorityRequest request) {
        log.info("REST: Create authority request from SUPER_USER: {}", superUserId);
        
        AuthorityResponse response = authorityService.createAuthority(superUserId, request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Authority created successfully"));
    }

    /**
     * Get all authorities
     * GET /api/authorities
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<AuthorityResponse>>> getAllAuthorities() {
        log.info("REST: Get all authorities request");
        
        List<AuthorityResponse> response = authorityService.getAllAuthorities();
        
        return ResponseEntity.ok(ApiResponse.success(response, "Authorities retrieved successfully"));
    }

    /**
     * Get authority by ID
     * GET /api/authorities/{authorityId}
     */
    @GetMapping("/{authorityId}")
    public ResponseEntity<ApiResponse<AuthorityResponse>> getAuthorityById(
            @PathVariable @NotNull Integer authorityId) {
        log.info("REST: Get authority by ID: {}", authorityId);
        
        AuthorityResponse response = authorityService.getAuthorityById(authorityId);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Authority retrieved successfully"));
    }

    /**
     * Get authority by name
     * GET /api/authorities/name/{name}
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<ApiResponse<AuthorityResponse>> getAuthorityByName(
            @PathVariable String name) {
        log.info("REST: Get authority by name: {}", name);
        
        AuthorityResponse response = authorityService.getAuthorityByName(name);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Authority retrieved successfully"));
    }

    /**
     * Get authorities by resource
     * GET /api/authorities/resource/{resource}
     */
    @GetMapping("/resource/{resource}")
    public ResponseEntity<ApiResponse<List<AuthorityResponse>>> getAuthoritiesByResource(
            @PathVariable String resource) {
        log.info("REST: Get authorities by resource: {}", resource);
        
        List<AuthorityResponse> response = authorityService.getAuthoritiesByResource(resource);
        
        return ResponseEntity.ok(ApiResponse.success(response, 
                "Authorities for resource retrieved successfully"));
    }

    /**
     * Update authority
     * PUT /api/authorities/{authorityId}
     * Requires: SUPER_USER role
     */
    @PutMapping("/{authorityId}")
    public ResponseEntity<ApiResponse<AuthorityResponse>> updateAuthority(
            @RequestHeader("X-User-Id") Long superUserId,
            @PathVariable @NotNull Integer authorityId,
            @Valid @RequestBody AuthorityRequest request) {
        log.info("REST: Update authority request from SUPER_USER: {}", superUserId);
        
        AuthorityResponse response = authorityService.updateAuthority(superUserId, authorityId, request);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Authority updated successfully"));
    }

    /**
     * Delete authority
     * DELETE /api/authorities/{authorityId}
     * Requires: SUPER_USER role
     */
    @DeleteMapping("/{authorityId}")
    public ResponseEntity<ApiResponse<Void>> deleteAuthority(
            @RequestHeader("X-User-Id") Long superUserId,
            @PathVariable @NotNull Integer authorityId) {
        log.info("REST: Delete authority request from SUPER_USER: {}", superUserId);
        
        authorityService.deleteAuthority(superUserId, authorityId);
        
        return ResponseEntity.ok(ApiResponse.success(null, "Authority deleted successfully"));
    }

    /**
     * Assign authorities to role
     * PUT /api/authorities/assign-to-role
     * Requires: SUPER_USER role
     */
    @PutMapping("/assign-to-role")
    public ResponseEntity<ApiResponse<Void>> assignAuthoritiesToRole(
            @RequestHeader("X-User-Id") Long superUserId,
            @Valid @RequestBody RoleAuthorityAssignmentRequest request) {
        log.info("REST: Assign authorities to role request from SUPER_USER: {}", superUserId);
        
        authorityService.assignAuthoritiesToRole(superUserId, request);
        
        return ResponseEntity.ok(ApiResponse.success(null, "Authorities assigned to role successfully"));
    }

    /**
     * Get authorities by role
     * GET /api/authorities/role/{roleId}
     */
    @GetMapping("/role/{roleId}")
    public ResponseEntity<ApiResponse<Set<AuthorityResponse>>> getAuthoritiesByRole(
            @PathVariable @NotNull Integer roleId) {
        log.info("REST: Get authorities by role: {}", roleId);
        
        Set<AuthorityResponse> response = authorityService.getAuthoritiesByRole(roleId);
        
        return ResponseEntity.ok(ApiResponse.success(response, 
                "Authorities for role retrieved successfully"));
    }

    /**
     * Get all authorities for a user (from roles + direct assignments)
     * GET /api/authorities/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Set<AuthorityResponse>>> getAllUserAuthorities(
            @PathVariable @NotNull Long userId) {
        log.info("REST: Get all authorities for user: {}", userId);
        
        Set<AuthorityResponse> response = authorityService.getAllUserAuthorities(userId);
        
        return ResponseEntity.ok(ApiResponse.success(response, 
                "User authorities retrieved successfully"));
    }

    /**
     * Check if user has specific authority
     * GET /api/authorities/user/{userId}/has/{authorityName}
     */
    @GetMapping("/user/{userId}/has/{authorityName}")
    public ResponseEntity<ApiResponse<Boolean>> userHasAuthority(
            @PathVariable @NotNull Long userId,
            @PathVariable String authorityName) {
        log.info("REST: Check if user {} has authority: {}", userId, authorityName);
        
        boolean hasAuthority = authorityService.userHasAuthority(userId, authorityName);
        
        return ResponseEntity.ok(ApiResponse.success(hasAuthority, 
                hasAuthority ? "User has authority" : "User does not have authority"));
    }
}