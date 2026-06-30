package com.dut.erp.service.impl;

import com.dut.erp.constant.SortingConstants;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.UpdateUserRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.UserBaseResponse;
import com.dut.erp.entity.User;
import com.dut.erp.exception.ResourceNotFoundException;
import com.dut.erp.mapper.UserMapper;
import com.dut.erp.repository.UserRepository;
import com.dut.erp.repository.RoleRepository;
import com.dut.erp.service.UserService;
import com.dut.erp.util.SearchUtils;
import java.util.UUID;
import com.dut.erp.dto.response.OrganizationMemberResponse;
import com.dut.erp.dto.response.RoleBaseResponse;
import com.dut.erp.entity.Role;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.dut.erp.dto.request.ChangePasswordRequest;
import com.dut.erp.exception.BadRequestException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public PagedEntityResponse<OrganizationMemberResponse> searchUsersByOrganizationId(
      UUID organizationId, String query, PaginationRequest paginationRequest) {
    String normalizedQuery = SearchUtils.normalizeOptionalFilter(query);

    Pageable pageable =
        PageRequest.of(
            paginationRequest.page() - 1,
            paginationRequest.limit(),
            Sort.by(Sort.Order.asc("firstName"), Sort.Order.asc("lastName")));
    Page<User> userPage;
    if (normalizedQuery == null) {
      userPage = userRepository.findAllByOrganizationsId(organizationId, pageable);
    } else {
      String escapedQuery = SearchUtils.escapeLikePattern(normalizedQuery);
      userPage =
          userRepository.searchByOrganizationsIdAndQuery(organizationId, escapedQuery, pageable);
    }

    Page<OrganizationMemberResponse> userResponses = userPage.map(user -> {
      List<RoleBaseResponse> roles = user.getRoles().stream()
          .filter(role -> role.getOrganization() != null && role.getOrganization().getId().equals(organizationId))
          .map(role -> new RoleBaseResponse(role.getId(), role.getName()))
          .toList();

      return new OrganizationMemberResponse(
          user.getId(),
          user.getEmail(),
          user.getFirstName(),
          user.getLastName(),
          roles,
          "Active",
          "-"
      );
    });
    log.info(
        "Fetched {} users for organization {} (hasQuery={}, total elements: {}, total pages: {})",
        userResponses.getNumberOfElements(),
        organizationId,
        normalizedQuery != null,
        userPage.getTotalElements(),
        userPage.getTotalPages());

    return PagedEntityResponse.from(userResponses);
  }

  @Override
  @Transactional
  public UserBaseResponse updateUser(UUID userId, UpdateUserRequest request) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () -> {
                  log.warn("User with ID {} not found", userId);
                  return new ResourceNotFoundException("User not found with id: " + userId);
                });

    user.setFirstName(request.firstName());
    user.setLastName(request.lastName());

    user = userRepository.save(user);
    log.info("User {} updated successfully", userId);

    return userMapper.toUserBaseResponse(user);
  }

  @Override
  public OrganizationMemberResponse getUserByIdAndOrganizationId(UUID userId, UUID organizationId) {
    User user = userRepository.findByIdWithRolesAndOrganizations(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

    boolean belongsToOrg = user.getOrganizations().stream()
        .anyMatch(org -> org.getId().equals(organizationId));
    if (!belongsToOrg) {
      log.warn("User {} does not belong to organization {}", userId, organizationId);
      throw new ResourceNotFoundException("User not found in this organization");
    }

    List<RoleBaseResponse> roles = user.getRoles().stream()
        .filter(role -> role.getOrganization() != null && role.getOrganization().getId().equals(organizationId))
        .map(role -> new RoleBaseResponse(role.getId(), role.getName()))
        .toList();

    return new OrganizationMemberResponse(
        user.getId(),
        user.getEmail(),
        user.getFirstName(),
        user.getLastName(),
        roles,
        "Active",
        "-"
    );
  }

  @Override
  @Transactional
  public OrganizationMemberResponse updateUserRoles(UUID userId, UUID organizationId, List<UUID> roleIds) {
    User user = userRepository.findByIdWithRolesAndOrganizations(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

    boolean belongsToOrg = user.getOrganizations().stream()
        .anyMatch(org -> org.getId().equals(organizationId));
    if (!belongsToOrg) {
      log.warn("User {} does not belong to organization {}", userId, organizationId);
      throw new ResourceNotFoundException("User not found in this organization");
    }

    // 1. Remove all existing roles of this user for this organization
    user.getRoles().removeIf(role -> role.getOrganization() != null && role.getOrganization().getId().equals(organizationId));

    // 2. Add new roles
    for (UUID roleId : roleIds) {
      Role role = roleRepository.findById(roleId)
          .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));
      if (role.getOrganization() == null || !role.getOrganization().getId().equals(organizationId)) {
        throw new com.dut.erp.exception.BadRequestException("Role " + roleId + " does not belong to organization " + organizationId);
      }
      user.getRoles().add(role);
    }

    user = userRepository.save(user);

    List<RoleBaseResponse> roles = user.getRoles().stream()
        .filter(role -> role.getOrganization() != null && role.getOrganization().getId().equals(organizationId))
        .map(role -> new RoleBaseResponse(role.getId(), role.getName()))
        .toList();

    return new OrganizationMemberResponse(
        user.getId(),
        user.getEmail(),
        user.getFirstName(),
        user.getLastName(),
        roles,
        "Active",
        "-"
    );
  }

  @Override
  @Transactional
  public void removeUserFromOrganization(UUID userId, UUID organizationId) {
    User user = userRepository.findByIdWithRolesAndOrganizations(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

    boolean belongsToOrg = user.getOrganizations().stream()
        .anyMatch(org -> org.getId().equals(organizationId));
    if (!belongsToOrg) {
      log.warn("User {} does not belong to organization {}", userId, organizationId);
      throw new ResourceNotFoundException("User not found in this organization");
    }

    // Remove the organization association
    user.getOrganizations().removeIf(org -> org.getId().equals(organizationId));

    // Remove all roles associated with this organization
    user.getRoles().removeIf(role -> role.getOrganization() != null && role.getOrganization().getId().equals(organizationId));

    userRepository.save(user);
    log.info("User {} removed from organization {}", userId, organizationId);
  }

  @Override
  @Transactional
  public void changePassword(UUID userId, ChangePasswordRequest request) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

    if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
      log.warn("Password change failed for user {}: old password does not match", userId);
      throw new BadRequestException("Mật khẩu cũ không chính xác.");
    }

    user.setPassword(passwordEncoder.encode(request.newPassword()));
    userRepository.save(user);
    log.info("Password changed successfully for user {}", userId);
  }
}
