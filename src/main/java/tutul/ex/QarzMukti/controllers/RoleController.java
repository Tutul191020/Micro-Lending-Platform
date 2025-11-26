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
import tutul.ex.QarzMukti.dto.RoleRequest;
import tutul.ex.QarzMukti.dto.RoleResponse;
import tutul.ex.QarzMukti.services.RoleService;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Validated
public class RoleController {

    private final RoleService roleService;

    /**
     * Create a new role
     * POST /api/roles
     */
    @PostMapping
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(
            @Valid @RequestBody RoleRequest request) {
        log.info("REST: Create role request received: {}", request.getName());
        
        RoleResponse response = roleService.createRole(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Role created successfully"));
    }

    /**
     * Get role by ID
     * GET /api/roles/{roleId}
     */
    @GetMapping("/{roleId}")
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleById(
            @PathVariable @NotNull Integer roleId) {
        log.info("REST: Get role by ID: {}", roleId);
        
        RoleResponse response = roleService.getRoleById(roleId);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Role retrieved successfully"));
    }

    /**
     * Get role by name
     * GET /api/roles/name/{name}
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleByName(@PathVariable String name) {
        log.info("REST: Get role by name: {}", name);
        
        RoleResponse response = roleService.getRoleByName(name);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Role retrieved successfully"));
    }

    /**
     * Get all roles
     * GET /api/roles
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRoles() {
        log.info("REST: Get all roles request received");
        
        List<RoleResponse> response = roleService.getAllRoles();
        
        return ResponseEntity.ok(ApiResponse.success(response, "Roles retrieved successfully"));
    }

    /**
     * Update role
     * PUT /api/roles/{roleId}
     */
    @PutMapping("/{roleId}")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
            @PathVariable @NotNull Integer roleId,
            @Valid @RequestBody RoleRequest request) {
        log.info("REST: Update role request received for ID: {}", roleId);
        
        RoleResponse response = roleService.updateRole(roleId, request);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Role updated successfully"));
    }

    /**
     * Delete role
     * DELETE /api/roles/{roleId}
     */
    @DeleteMapping("/{roleId}")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable @NotNull Integer roleId) {
        log.info("REST: Delete role request received for ID: {}", roleId);
        
        roleService.deleteRole(roleId);
        
        return ResponseEntity.ok(ApiResponse.success(null, "Role deleted successfully"));
    }

    /**
     * Check if role exists
     * GET /api/roles/exists/{name}
     */
    @GetMapping("/exists/{name}")
    public ResponseEntity<ApiResponse<Boolean>> roleExists(@PathVariable String name) {
        log.info("REST: Check role exists: {}", name);
        
        boolean exists = roleService.roleExists(name);
        
        return ResponseEntity.ok(ApiResponse.success(exists, 
                exists ? "Role exists" : "Role does not exist"));
    }

    /**
     * Get user count for role
     * GET /api/roles/{roleId}/user-count
     */
    @GetMapping("/{roleId}/user-count")
    public ResponseEntity<ApiResponse<Long>> getUserCountByRole(
            @PathVariable @NotNull Integer roleId) {
        log.info("REST: Get user count for role: {}", roleId);
        
        long count = roleService.getUserCountByRole(roleId);
        
        return ResponseEntity.ok(ApiResponse.success(count, "User count retrieved successfully"));
    }
}