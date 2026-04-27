package com.dut.erp.service.impl;

import com.dut.erp.dto.common.PermissionActionPair;
import com.dut.erp.dto.request.CreateRoleRequest;
import com.dut.erp.dto.response.RoleResponse;
import com.dut.erp.entity.Action;
import com.dut.erp.entity.Organization;
import com.dut.erp.entity.Permission;
import com.dut.erp.entity.Role;
import com.dut.erp.mapper.RoleMapper;
import com.dut.erp.repository.ActionRepository;
import com.dut.erp.repository.PermissionRepository;
import com.dut.erp.repository.RoleRepository;
import com.dut.erp.service.OrganizationService;
import com.dut.erp.service.RoleService;
import jakarta.persistence.EntityNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleServiceImpl implements RoleService {
  private final RoleRepository roleRepository;
  private final PermissionRepository permissionRepository;
  private final ActionRepository actionRepository;
  private final RoleMapper roleMapper;

  private final OrganizationService organizationService;

  @Override
  public List<RoleResponse> getRolesByUserIdAndOrganizationId(UUID userId, UUID organizationId) {
    return roleRepository.findRolesByUserIdAndOrganizationId(userId, organizationId).stream()
        .map(roleMapper::toRoleResponse)
        .toList();
  }

  @Override
  public List<RoleResponse> getAllRolesByOrganizationId(UUID organizationId) {
    return roleRepository.findAllByOrganizationId(organizationId).stream()
        .map(roleMapper::toRoleResponse)
        .toList();
  }

  @Override
  public RoleResponse getRoleById(UUID roleId) {
    return roleMapper.toRoleResponse(findRoleById(roleId));
  }

  @Override
  @Transactional
  public RoleResponse addRole(CreateRoleRequest request) {
    Role role = Role.builder().name(request.name()).build();

    Organization organization = organizationService.getOrganizationById(request.organizationId());
    role.setOrganization(organization);

    assignPermissionsToRole(role, request.permissions());

    Role savedRole = roleRepository.save(role);
    return roleMapper.toRoleResponse(savedRole);
  }

  private Role findRoleById(UUID roleId) {
    return roleRepository
        .findById(roleId)
        .orElseThrow(() -> new IllegalArgumentException("Role not found"));
  }

  private void assignPermissionsToRole(Role role, Set<PermissionActionPair> pairs) {
    Set<UUID> permissionIds = new HashSet<>();
    Set<UUID> actionIds = new HashSet<>();

    for (PermissionActionPair pair : pairs) {
      permissionIds.add(pair.permissionId());
      actionIds.add(pair.actionId());
    }

    Map<UUID, Permission> permissionMap =
        permissionRepository.findAllByIdsIn(permissionIds).stream()
            .collect(Collectors.toMap(Permission::getId, Function.identity()));

    Map<UUID, Action> actionMap =
        actionRepository.findAllByIdsIn(actionIds).stream()
            .collect(Collectors.toMap(Action::getId, Function.identity()));

    for (PermissionActionPair pair : pairs) {
      Permission permission =
          Optional.ofNullable(permissionMap.get(pair.permissionId()))
              .orElseThrow(
                  () ->
                      new EntityNotFoundException("Permission not found: " + pair.permissionId()));

      Action action =
          Optional.ofNullable(actionMap.get(pair.actionId()))
              .orElseThrow(
                  () -> new EntityNotFoundException("Action not found: " + pair.actionId()));

      permission.getActions().add(action);
      role.getPermissions().add(permission);
    }
  }
}
