package tutul.ex.QarzMukti.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tutul.ex.QarzMukti.constants.ApiEndpoints;
import tutul.ex.QarzMukti.dto.ApiResponse;
import tutul.ex.QarzMukti.dto.RoleRequest;
import tutul.ex.QarzMukti.dto.RoleResponse;
import tutul.ex.QarzMukti.services.RoleService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(ApiEndpoints.Roles.BASE)
@RequiredArgsConstructor
@Validated
public class RoleController {

    private final RoleService roleService;

    @PostMapping(ApiEndpoints.Roles.BASE)
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(@Valid @RequestBody RoleRequest request) {
        log.info("Creating new role: {}", request.getName());
        RoleResponse response = roleService.createRole(request);
        log.info("Role created successfully - roleId: {}, name: {}", response.getId(), response.getName());
        return new ResponseEntity<>(ApiResponse.success(response, "Role created successfully"), HttpStatus.CREATED);
    }

    @GetMapping(ApiEndpoints.Roles.BY_ID)
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleById(@PathVariable Integer roleId) {
        log.info("Fetching role by roleId: {}", roleId);
        RoleResponse response = roleService.getRoleById(roleId);
        return new ResponseEntity<>(ApiResponse.success(response, "Role retrieved successfully"), HttpStatus.OK);
    }

    @GetMapping(ApiEndpoints.Roles.BY_NAME)
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleByName(@PathVariable String name) {
        log.info("Fetching role by name: {}", name);
        RoleResponse response = roleService.getRoleByName(name);
        return new ResponseEntity<>(ApiResponse.success(response, "Role retrieved successfully"), HttpStatus.OK);
    }

    @GetMapping(ApiEndpoints.Roles.BASE)
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRoles() {
        log.info("Fetching all roles");
        List<RoleResponse> response = roleService.getAllRoles();
        log.info("Found {} roles", response.size());
        return new ResponseEntity<>(ApiResponse.success(response, "Roles retrieved successfully"), HttpStatus.OK);
    }

    @PutMapping(ApiEndpoints.Roles.BY_ID)
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
            @PathVariable Integer roleId,
            @Valid @RequestBody RoleRequest request) {
        log.info("Updating role - roleId: {}, newName: {}", roleId, request.getName());
        RoleResponse response = roleService.updateRole(roleId, request);
        log.info("Role updated successfully - roleId: {}", roleId);
        return new ResponseEntity<>(ApiResponse.success(response, "Role updated successfully"), HttpStatus.OK);
    }

    @DeleteMapping(ApiEndpoints.Roles.BY_ID)
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable Integer roleId) {
        log.info("Deleting role - roleId: {}", roleId);
        roleService.deleteRole(roleId);
        log.info("Role deleted successfully - roleId: {}", roleId);
        return new ResponseEntity<>(ApiResponse.success(null, "Role deleted successfully"), HttpStatus.OK);
    }

    @GetMapping(ApiEndpoints.Roles.EXISTS)
    public ResponseEntity<ApiResponse<Boolean>> roleExists(@PathVariable String name) {
        log.info("Checking if role exists: {}", name);
        boolean exists = roleService.roleExists(name);
        log.info("Role {} existence check result: {}", name, exists);
        return new ResponseEntity<>(ApiResponse.success(exists, exists ? "Role exists" : "Role does not exist"),
                HttpStatus.OK);
    }

    @GetMapping(ApiEndpoints.Roles.USER_COUNT)
    public ResponseEntity<ApiResponse<Long>> getUserCountByRole(@PathVariable Integer roleId) {
        log.info("Fetching user count for roleId: {}", roleId);
        long count = roleService.getUserCountByRole(roleId);
        log.info("Role {} has {} users", roleId, count);
        return new ResponseEntity<>(ApiResponse.success(count, "User count retrieved successfully"), HttpStatus.OK);
    }
}