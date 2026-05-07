package com.dut.erp.service.impl;

import com.dut.erp.constant.SortingConstants;
import com.dut.erp.dto.common.SortField;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.RoleBaseResponse;
import com.dut.erp.dto.response.RoleResponse;
import com.dut.erp.entity.Role;
import com.dut.erp.exception.ResourceNotFoundException;
import com.dut.erp.mapper.RoleMapper;
import com.dut.erp.repository.RoleRepository;
import com.dut.erp.service.RoleService;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
  private final RoleRepository roleRepository;
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
  public RoleResponse getRoleById(UUID roleId) {
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
}
