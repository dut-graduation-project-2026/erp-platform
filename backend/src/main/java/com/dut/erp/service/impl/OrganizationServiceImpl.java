package com.dut.erp.service.impl;

import com.dut.erp.dto.response.OrganizationResponse;
import com.dut.erp.entity.Organization;
import com.dut.erp.entity.Role;
import com.dut.erp.entity.User;
import com.dut.erp.exception.BadRequestException;
import com.dut.erp.exception.ResourceNotFoundException;
import com.dut.erp.mapper.OrganizationMapper;
import com.dut.erp.repository.OrganizationRepository;
import com.dut.erp.repository.RoleRepository;
import com.dut.erp.repository.UserRepository;
import com.dut.erp.service.OrganizationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganizationServiceImpl implements OrganizationService {
  private final OrganizationMapper organizationMapper;
  private final OrganizationRepository organizationRepository;
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;

  @Override
  public List<OrganizationResponse> getOrganizationsByUserId(UUID userId) {
    return organizationRepository.findAllByUserId(userId).stream()
        .map(organizationMapper::toOrganizationResponse)
        .toList();
  }

  @Override
  public OrganizationResponse getOrganizationById(UUID organizationId) {
    return organizationMapper.toOrganizationResponse(findOrganizationById(organizationId));
  }

  @Override
  public Organization findOrganizationById(UUID organizationId) {
    return organizationRepository
        .findById(organizationId)
        .orElseThrow(
            () -> {
              log.warn("Organization with ID {} not found", organizationId);
              return new ResourceNotFoundException(
                  "Organization not found with id: " + organizationId);
            });
  }

  @Override
  @Transactional
  public Organization addMemberToOrganization(UUID organizationId, UUID userId, UUID roleId) {
    Organization organization = findOrganizationById(organizationId);

    User user =
        userRepository
            .findByIdWithRolesAndOrganizations(userId)
            .orElseThrow(
                () -> {
                  log.warn("User with ID {} not found", userId);
                  return new ResourceNotFoundException("User not found with id: " + userId);
                });
    Role role =
        roleRepository
            .findByIdWithOrganization(roleId)
            .orElseThrow(
                () -> {
                  log.warn("Role with ID {} not found", roleId);
                  return new ResourceNotFoundException("Role not found with id: " + roleId);
                });

    if (!organizationId.equals(role.getOrganization().getId())) {
      log.warn("Role {} does not belong to organization {}", roleId, organizationId);
      throw new BadRequestException("Role does not belong to the specified organization.");
    }

    boolean alreadyMember =
        user.getOrganizations().stream().anyMatch(org -> org.getId().equals(organizationId));
    if (alreadyMember) {
      log.warn("User {} is already a member of organization {}", userId, organizationId);
      throw new BadRequestException("User is already a member of this organization.");
    }

    user.getOrganizations().add(organization);
    organization.getUsers().add(user);
    user.getRoles().add(role);

    userRepository.save(user);
    return organization;
  }
}
