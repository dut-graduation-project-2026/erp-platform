package com.dut.erp.service.impl;

import com.dut.erp.constant.SortingConstants;
import com.dut.erp.dto.common.SortField;
import com.dut.erp.dto.request.CreateRoleRequest;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.UpdateRoleRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.RoleBaseResponse;
import com.dut.erp.dto.response.RoleResponse;
import com.dut.erp.entity.Organization;
import com.dut.erp.entity.Permission;
import com.dut.erp.entity.Role;
import com.dut.erp.exception.BadRequestException;
import com.dut.erp.exception.ResourceAlreadyExistsException;
import com.dut.erp.exception.ResourceNotFoundException;
import com.dut.erp.mapper.RoleMapper;
import com.dut.erp.repository.OrganizationRepository;
import com.dut.erp.repository.PermissionRepository;
import com.dut.erp.repository.RoleRepository;
import com.dut.erp.service.RoleService;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service("roleService")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleServiceImpl implements RoleService {

  private final OrganizationRepository organizationRepository;
  private final RoleRepository roleRepository;
  private final PermissionRepository permissionRepository;
  private final RoleMapper roleMapper;

  @Override
  public PagedEntityResponse<RoleBaseResponse> getRolesByOrganizationId(
      UUID organizationId, PaginationRequest paginationRequest) {
    log.info(
        "Fetching roles for organization {} with pagination: page={}, limit={}",
        organizationId,
        paginationRequest.page(),
        paginationRequest.limit());

    Pageable pageable =
        PageRequest.of(
            paginationRequest.page() - 1,
            paginationRequest.limit(),
            SortingConstants.customEntitiesSort(
                SortField.asc("updatedAt"), SortField.asc("createdAt"), SortField.asc("name")));

    Page<UUID> roleIds = roleRepository.findRoleIdsByOrganizationId(organizationId, pageable);
    return mapToPagedRoleBaseResponse(roleIds, pageable);
  }

  @Override
  @Transactional
  public RoleResponse createRole(UUID organizationId, CreateRoleRequest request) {
    Organization organization = findOrganizationById(organizationId);
    assertRoleNameAvailable(request.name(), organizationId);

    Set<Permission> permissions = resolvePermissions(request.permissionIds(), organizationId);

    Role role =
        Role.builder()
            .name(request.name())
            .organization(organization)
            .permissions(permissions)
            .build();
    role = roleRepository.save(role);

    log.info("Created role {} in organization {}", role.getId(), organizationId);

    return roleMapper.toRoleResponse(role);
  }

  @Override
  public RoleResponse getRoleByIdWithOrganizationAndPermissionAndModule(UUID roleId) {
    log.info("Fetching role with ID {}", roleId);
    Role role =
        roleRepository
            .findByIdWithOrganizationAndPermissionAndModule(roleId)
            .orElseThrow(
                () -> {
                  log.warn("Role with ID {} not found", roleId);
                  return new ResourceNotFoundException("Role not found with id: " + roleId);
                });
    return roleMapper.toRoleResponse(role);
  }

  @Override
  public boolean isRoleBelongsToOrganization(UUID roleId, UUID organizationId) {
    // Use findByIdWithOrganization (JOIN FETCH) to avoid LazyInitializationException
    // inside @PreAuthorize SpEL context (which runs outside of any transaction).
    // Returns false (→ 403) instead of throwing an exception (→ 400).
    return roleRepository.findByIdWithOrganization(roleId)
        .map(role -> role.getOrganization() != null
            && role.getOrganization().getId().equals(organizationId))
        .orElse(false);
  }


  @Override
  @Transactional
  public RoleResponse updateRole(UUID roleId, UUID organizationId, UpdateRoleRequest request) {
    findOrganizationById(organizationId);
    Role role = findRoleById(roleId);
    verifyRoleBelongsToOrganization(roleId, organizationId);

    Set<UUID> requestPermIds = request.permissionIds() != null ? request.permissionIds() : Set.of();
    Set<Permission> resolvedPerms = resolvePermissions(requestPermIds, organizationId);

    if (!role.getName().equals(request.name())) {
      assertRoleNameAvailable(request.name(), organizationId);
    }

    role.setName(request.name());
    role.setPermissions(resolvedPerms);
    role = roleRepository.save(role);

    log.info("Updated role {} in organization {}", roleId, organizationId);

    return roleMapper.toRoleResponse(role);
  }

  @Override
  @Transactional
  public void deleteRole(UUID roleId, UUID organizationId) {
    findOrganizationById(organizationId);
    Role role = findRoleById(roleId);
    verifyRoleBelongsToOrganization(roleId, organizationId);

    roleRepository.delete(role);
    log.info("Deleted role {} from organization {}", roleId, organizationId);
  }

  private void assertRoleNameAvailable(String name, UUID organizationId) {
    if (roleRepository.findByNameAndOrganizationId(name, organizationId).isPresent()) {
      log.warn("Role name '{}' already exists in organization {}", name, organizationId);
      throw new ResourceAlreadyExistsException(
          "Role with this name already exists in the specified organization.");
    }
  }

  private Set<Permission> resolvePermissions(Set<UUID> permissionIds, UUID organizationId) {
    if (permissionIds == null || permissionIds.isEmpty()) {
      return new HashSet<>();
    }

    List<Permission> permissions = permissionRepository.findAllById(permissionIds);
    if (permissions.size() != permissionIds.size()) {
      log.warn("One or more permissions not found for organization {}", organizationId);
      throw new ResourceNotFoundException("One or more permissions were not found.");
    }

    return new HashSet<>(permissions);
  }

  private PagedEntityResponse<RoleBaseResponse> mapToPagedRoleBaseResponse(
      Page<UUID> roleIdsPage, Pageable pageable) {
    if (roleIdsPage.isEmpty()) {
      return PagedEntityResponse.from(Page.empty(pageable));
    }

    Map<UUID, Role> roleMap =
        roleRepository.findAllByIdIn(roleIdsPage.getContent()).stream()
            .collect(Collectors.toMap(Role::getId, Function.identity()));

    List<RoleBaseResponse> responses =
        roleIdsPage.getContent().stream()
            .map(roleMap::get)
            .filter(Objects::nonNull)
            .map(roleMapper::toRoleBaseResponse)
            .collect(Collectors.toList());

    return PagedEntityResponse.from(
        new PageImpl<>(responses, pageable, roleIdsPage.getTotalElements()));
  }

  private Organization findOrganizationById(UUID organizationId) {
    return organizationRepository
        .findById(organizationId)
        .orElseThrow(
            () -> {
              log.warn("Organization with ID {} not found", organizationId);
              return new ResourceNotFoundException(
                  "Organization not found with id: " + organizationId);
            });
  }

  private Role findRoleById(UUID roleId) {
    return roleRepository
        .findById(roleId)
        .orElseThrow(
            () -> {
              log.warn("Role with ID {} not found", roleId);
              return new ResourceNotFoundException("Role not found with id: " + roleId);
            });
  }

  private void verifyRoleBelongsToOrganization(UUID roleId, UUID organizationId) {
    if (!isRoleBelongsToOrganization(roleId, organizationId)) {
      log.warn("Role with ID {} does not belong to organization {}", roleId, organizationId);
      throw new BadRequestException("Role does not belong to the specified organization.");
    }
  }
}
