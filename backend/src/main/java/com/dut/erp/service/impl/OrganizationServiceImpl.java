package com.dut.erp.service.impl;

import com.dut.erp.dto.request.CreateOrganizationRequest;
import com.dut.erp.dto.request.UpdateOrganizationRequest;
import com.dut.erp.dto.response.OrganizationResponse;
import com.dut.erp.entity.Organization;
import com.dut.erp.entity.Permission;
import com.dut.erp.entity.Role;
import com.dut.erp.entity.User;
import com.dut.erp.exception.BadRequestException;
import com.dut.erp.exception.ResourceAlreadyExistsException;
import com.dut.erp.exception.ResourceNotFoundException;
import com.dut.erp.mapper.OrganizationMapper;
import com.dut.erp.repository.OrganizationRepository;
import com.dut.erp.repository.PermissionRepository;
import com.dut.erp.repository.RoleRepository;
import com.dut.erp.repository.UserRepository;
import com.dut.erp.service.OrganizationService;
import java.util.HashSet;
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
  private static final String DEFAULT_ADMIN_ROLE_NAME = "ADMIN";

  private final OrganizationMapper organizationMapper;
  private final OrganizationRepository organizationRepository;
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PermissionRepository permissionRepository;

  @Override
  @Transactional
  public OrganizationResponse createOrganization(UUID userId, CreateOrganizationRequest request) {
    if (organizationRepository.existsByTaxCode(request.taxCode())) {
      log.warn("Organization creation failed: tax code {} already exists", request.taxCode());
      throw new ResourceAlreadyExistsException("Organization with this tax code already exists.");
    }

    User creator =
        userRepository
            .findByIdWithRolesAndOrganizations(userId)
            .orElseThrow(
                () -> {
                  log.warn("User with ID {} not found", userId);
                  return new ResourceNotFoundException("User not found with id: " + userId);
                });

    Organization organization =
        Organization.builder()
            .name(request.name())
            .description(request.description())
            .address(request.address())
            .hotline(request.hotline())
            .taxCode(request.taxCode())
            .build();
    organization = organizationRepository.save(organization);

    List<Permission> permissions = permissionRepository.findAll();
    Role adminRole =
        Role.builder()
            .name(DEFAULT_ADMIN_ROLE_NAME)
            .organization(organization)
            .permissions(new HashSet<>(permissions))
            .build();
    adminRole = roleRepository.save(adminRole);

    creator.getOrganizations().add(organization);
    creator.getRoles().add(adminRole);
    userRepository.save(creator);

    OrganizationResponse mappedResponse = organizationMapper.toOrganizationResponse(organization);
    return OrganizationResponse.builder()
        .id(mappedResponse.id())
        .name(mappedResponse.name())
        .description(mappedResponse.description())
        .hotline(mappedResponse.hotline())
        .address(mappedResponse.address())
        .taxCode(mappedResponse.taxCode())
        .role(DEFAULT_ADMIN_ROLE_NAME)
        .build();
  }

  @Override
  public List<OrganizationResponse> getOrganizationsByUserId(UUID userId) {
    User user = userRepository.findByIdWithRolesAndOrganizations(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

    return user.getOrganizations().stream()
        .map(org -> {
          String roleName = user.getRoles().stream()
              .filter(role -> role.getOrganization() != null && role.getOrganization().getId().equals(org.getId()))
              .map(Role::getName)
              .findFirst()
              .orElse("MEMBER");
          
          OrganizationResponse mappedResponse = organizationMapper.toOrganizationResponse(org);
          return OrganizationResponse.builder()
              .id(mappedResponse.id())
              .name(mappedResponse.name())
              .description(mappedResponse.description())
              .hotline(mappedResponse.hotline())
              .address(mappedResponse.address())
              .taxCode(mappedResponse.taxCode())
              .role(roleName)
              .build();
        })
        .toList();
  }

  @Override
  public OrganizationResponse getOrganizationById(UUID organizationId) {
    return organizationMapper.toOrganizationResponse(findOrganizationById(organizationId));
  }

  @Override
  @Transactional
  public OrganizationResponse updateOrganization(
      UUID organizationId, UpdateOrganizationRequest request) {
    Organization organization = findOrganizationById(organizationId);

    // Check if new tax code already exists (if different from current)
    if (!organization.getTaxCode().equals(request.taxCode())
        && organizationRepository.existsByTaxCode(request.taxCode())) {
      log.warn("Organization update failed: tax code {} already exists", request.taxCode());
      throw new ResourceAlreadyExistsException("Organization with this tax code already exists.");
    }

    organization.setName(request.name());
    organization.setDescription(request.description());
    organization.setAddress(request.address());
    organization.setHotline(request.hotline());
    organization.setTaxCode(request.taxCode());

    organization = organizationRepository.save(organization);
    log.info("Organization {} updated", organizationId);

    return organizationMapper.toOrganizationResponse(organization);
  }

  @Transactional
  private Organization addMemberToOrganization(UUID organizationId, UUID userId, UUID roleId) {
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
