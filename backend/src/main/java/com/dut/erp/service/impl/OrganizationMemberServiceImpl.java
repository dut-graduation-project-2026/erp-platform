package com.dut.erp.service.impl;

import com.dut.erp.dto.request.UpdateOrganizationMemberRolesRequest;
import com.dut.erp.dto.response.OrganizationMemberResponse;
import com.dut.erp.dto.response.RoleResponse;
import com.dut.erp.entity.Organization;
import com.dut.erp.entity.Role;
import com.dut.erp.entity.User;
import com.dut.erp.exception.BadRequestException;
import com.dut.erp.exception.ResourceNotFoundException;
import com.dut.erp.mapper.RoleMapper;
import com.dut.erp.repository.OrganizationRepository;
import com.dut.erp.repository.RoleRepository;
import com.dut.erp.repository.UserRepository;
import com.dut.erp.service.OrganizationMemberService;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.constant.SortingConstants;
import com.dut.erp.util.SearchUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganizationMemberServiceImpl implements OrganizationMemberService {
  private final UserRepository userRepository;
  private final OrganizationRepository organizationRepository;
  private final RoleRepository roleRepository;
  private final RoleMapper roleMapper;

  @Override
  public PagedEntityResponse<OrganizationMemberResponse> getMembers(
      UUID organizationId, String query, PaginationRequest paginationRequest) {
    String normalizedQuery = SearchUtils.normalizeOptionalFilter(query);

    Pageable pageable =
        PageRequest.of(
            paginationRequest.page() - 1,
            paginationRequest.limit(),
            SortingConstants.DEFAULT_ENTITIES_SORT);
    Page<User> userPage;
    if (normalizedQuery == null) {
      userPage = userRepository.findAllByOrganizationsId(organizationId, pageable);
    } else {
      String escapedQuery = SearchUtils.escapeLikePattern(normalizedQuery);
      userPage =
          userRepository.searchByOrganizationsIdAndQuery(organizationId, escapedQuery, pageable);
    }

    Page<OrganizationMemberResponse> responses = userPage.map(user -> {
      List<RoleResponse> roles = user.getRoles().stream()
          .filter(role -> role.getOrganization().getId().equals(organizationId))
          .map(roleMapper::toRoleResponse)
          .collect(Collectors.toList());
      
      return OrganizationMemberResponse.builder()
          .id(user.getId())
          .email(user.getEmail())
          .firstName(user.getFirstName())
          .lastName(user.getLastName())
          .roles(roles)
          .status("Active")
          .lastLogin(null)
          .build();
    });

    return PagedEntityResponse.from(responses);
  }

  @Override
  public OrganizationMemberResponse getMemberById(UUID organizationId, UUID userId) {
    User user =
        userRepository
            .findByIdAndOrganizationsId(userId, organizationId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Member not found in the organization"));

    List<RoleResponse> roles =
        user.getRoles().stream()
            .filter(role -> role.getOrganization().getId().equals(organizationId))
            .map(roleMapper::toRoleResponse)
            .collect(Collectors.toList());

    return OrganizationMemberResponse.builder()
        .id(user.getId())
        .email(user.getEmail())
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .roles(roles)
        .status("Active") // Hardcoded as Active since they are in the organization
        .lastLogin(null) // Hardcoded for now
        .build();
  }

  @Override
  @Transactional
  public OrganizationMemberResponse updateMemberRoles(
      UUID organizationId, UUID userId, UpdateOrganizationMemberRolesRequest request) {
    User user =
        userRepository
            .findByIdAndOrganizationsId(userId, organizationId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Member not found in the organization"));

    // Remove old roles for this organization
    Set<Role> rolesToRemove =
        user.getRoles().stream()
            .filter(role -> role.getOrganization().getId().equals(organizationId))
            .collect(Collectors.toSet());
    user.getRoles().removeAll(rolesToRemove);

    // Add new roles
    if (request.roleIds() != null && !request.roleIds().isEmpty()) {
      List<Role> newRoles = roleRepository.findAllById(request.roleIds());
      for (Role role : newRoles) {
        if (!role.getOrganization().getId().equals(organizationId)) {
          throw new BadRequestException("Role " + role.getId() + " does not belong to the organization");
        }
      }
      user.getRoles().addAll(newRoles);
    }

    userRepository.save(user);

    List<RoleResponse> roleResponses =
        user.getRoles().stream()
            .filter(role -> role.getOrganization().getId().equals(organizationId))
            .map(roleMapper::toRoleResponse)
            .collect(Collectors.toList());

    return OrganizationMemberResponse.builder()
        .id(user.getId())
        .email(user.getEmail())
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .roles(roleResponses)
        .status("Active")
        .lastLogin(null)
        .build();
  }

  @Override
  @Transactional
  public void removeMember(UUID organizationId, UUID userId) {
    User user =
        userRepository
            .findByIdAndOrganizationsId(userId, organizationId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Member not found in the organization"));
    Organization organization =
        organizationRepository
            .findById(organizationId)
            .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

    user.getOrganizations().remove(organization);

    // Remove roles associated with this organization
    Set<Role> rolesToRemove =
        user.getRoles().stream()
            .filter(role -> role.getOrganization().getId().equals(organizationId))
            .collect(Collectors.toSet());
    user.getRoles().removeAll(rolesToRemove);

    userRepository.save(user);
    log.info("Removed user {} from organization {}", userId, organizationId);
  }
}
