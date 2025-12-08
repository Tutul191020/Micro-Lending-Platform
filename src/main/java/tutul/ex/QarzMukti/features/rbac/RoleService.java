package tutul.ex.QarzMukti.features.rbac;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tutul.ex.QarzMukti.shared.common.DuplicateResourceException;
import tutul.ex.QarzMukti.shared.common.ResourceNotFoundException;
import tutul.ex.QarzMukti.shared.common.ValidationException;
import tutul.ex.QarzMukti.shared.dto.RoleRequest;
import tutul.ex.QarzMukti.shared.dto.RoleResponse;
import tutul.ex.QarzMukti.shared.entities.Role;
import tutul.ex.QarzMukti.shared.repositories.RoleRepository;
import tutul.ex.QarzMukti.shared.repositories.UserRoleRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;


    @Transactional
    public RoleResponse createRole(RoleRequest request) {
        log.info("Creating new role: {}", request.getName());

        if (roleRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Role already exists: " + request.getName());
        }

        Role role = Role.builder()
                .name(request.getName())
                .build();

        role = roleRepository.save(role);

        log.info("Role created successfully: {}", role.getId());
        return mapToRoleResponse(role);
    }


    @Transactional(readOnly = true)
    public RoleResponse getRoleById(Integer roleId) {
        Role role = findRoleOrThrow(roleId);
        return mapToRoleResponse(role);
    }


    @Transactional(readOnly = true)
    public RoleResponse getRoleByName(String name) {
        Role role = roleRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + name));
        return mapToRoleResponse(role);
    }


    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::mapToRoleResponse)
                .collect(Collectors.toList());
    }


    @Transactional
    public RoleResponse updateRole(Integer roleId, RoleRequest request) {
        log.info("Updating role: {}", roleId);

        Role role = findRoleOrThrow(roleId);

        if (!role.getName().equals(request.getName())) {
            if (roleRepository.existsByName(request.getName())) {
                throw new DuplicateResourceException("Role name already exists: " + request.getName());
            }
            role.setName(request.getName());
        }

        role = roleRepository.save(role);

        log.info("Role updated successfully: {}", roleId);
        return mapToRoleResponse(role);
    }


    @Transactional
    public void deleteRole(Integer roleId) {
        log.info("Deleting role: {}", roleId);

        Role role = findRoleOrThrow(roleId);

        // Check if any users have this role
        long userCount = userRoleRepository.countByRoleId(roleId);
        if (userCount > 0) {
            throw new ValidationException(
                    String.format("Cannot delete role. %d user(s) are assigned to this role.", userCount)
            );
        }

        roleRepository.delete(role);
        log.info("Role deleted successfully: {}", roleId);
    }


    @Transactional(readOnly = true)
    public boolean roleExists(String name) {
        return roleRepository.existsByName(name);
    }


    @Transactional(readOnly = true)
    public long getUserCountByRole(Integer roleId) {
        findRoleOrThrow(roleId);
        return userRoleRepository.countByRoleId(roleId);
    }

    // ============= Helper Methods =============

    private Role findRoleOrThrow(Integer roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleId));
    }

    private RoleResponse mapToRoleResponse(Role role) {
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .build();
    }
}