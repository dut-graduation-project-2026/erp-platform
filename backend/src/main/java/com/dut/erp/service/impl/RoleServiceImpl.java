package com.dut.erp.service.impl;

import com.dut.erp.constant.SortingConstants;
import com.dut.erp.dto.request.CreateRoleRequest;
import com.dut.erp.dto.common.SortField;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.RoleBaseResponse;
import com.dut.erp.dto.response.RoleResponse;
import com.dut.erp.entity.Organization;
import com.dut.erp.entity.Permission;
import com.dut.erp.entity.Role;
import com.dut.erp.exception.ResourceAlreadyExistsException;
import com.dut.erp.exception.ResourceNotFoundException;
import com.dut.erp.mapper.RoleMapper;
import com.dut.erp.repository.OrganizationRepository;
import com.dut.erp.repository.PermissionRepository;
import com.dut.erp.repository.RoleRepository;
import com.dut.erp.service.RoleService;
import java.util.Collections;
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
@Service
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
    Pageable pageable =
        PageRequest.of(
            paginationRequest.page() - 1,
            paginationRequest.limit(),
            SortingConstants.customEntitiesSort(
                SortField.asc("updatedAt"), SortField.asc("createdAt"), SortField.asc("name")));
    log.info(
        "Fetching roles for organization {} with pagination: page={}, limit={}",
        organizationId,
        paginationRequest.page(),
        paginationRequest.limit());

    Page<UUID> roleIds = roleRepository.findRoleIdsByOrganizationId(organizationId, pageable);
    PagedEntityResponse<RoleBaseResponse> response = mapToPagedRoleBaseResponse(roleIds, pageable);
    return response;
  }

  @Override
  @Transactional
  public RoleResponse createRole(UUID organizationId, CreateRoleRequest request) {
    Organization organization = findOrganizationById(organizationId);

    if (roleRepository.findByNameAndOrganizationId(request.name(), organizationId).isPresent()) {
      log.warn(
          "Role creation failed: role name '{}' already exists in organization {}",
          request.name(),
          organizationId);
      throw new ResourceAlreadyExistsException(
          "Role with this name already exists in the specified organization.");
    }

    Set<UUID> permissionIds =
        request.permissionIds() == null ? Collections.emptySet() : request.permissionIds();
    List<Permission> permissions = permissionIds.isEmpty() ? List.of() : permissionRepository.findAllById(permissionIds);
    if (permissions.size() != permissionIds.size()) {
      log.warn(
          "Role creation failed: one or more permissions were not found for organization {}",
          organizationId);
      throw new ResourceNotFoundException("One or more permissions were not found.");
    }

    Role role =
        Role.builder()
            .name(request.name())
            .organization(organization)
            .permissions(new HashSet<>(permissions))
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

  private PagedEntityResponse<RoleBaseResponse> mapToPagedRoleBaseResponse(
      Page<UUID> roleIdsPage, Pageable pageable) {
    if (roleIdsPage.isEmpty()) {
      log.debug(
          "No roles found for organization with pagination: page={}, limit={}",
          pageable.getPageNumber() + 1,
          pageable.getPageSize());
      return PagedEntityResponse.from(Page.empty(pageable));
    }

    log.debug(
        "Mapping {} role IDs to RoleBaseResponse for pagination: page={}, limit={}",
        roleIdsPage.getNumberOfElements(),
        pageable.getPageNumber() + 1,
        pageable.getPageSize());
    Map<UUID, Role> roleMap =
        roleRepository.findAllByIdIn(roleIdsPage.getContent()).stream()
            .collect(Collectors.toMap(Role::getId, Function.identity()));

    List<RoleBaseResponse> roleBaseResponses =
        roleIdsPage.getContent().stream()
            .map(roleMap::get)
            .filter(Objects::nonNull)
            .map(roleMapper::toRoleBaseResponse)
            .collect(Collectors.toList());

    log.debug(
        "Mapped {} RoleBaseResponse objects for pagination: page={}, limit={}",
        roleBaseResponses.size(),
        pageable.getPageNumber() + 1,
        pageable.getPageSize());

    return PagedEntityResponse.from(
        new PageImpl<>(roleBaseResponses, pageable, roleIdsPage.getTotalElements()));
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
}
