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
import com.dut.erp.service.AuditLogService;
import com.dut.erp.service.OrganizationService;
import com.dut.erp.enums.ActionType;
import com.dut.erp.enums.EntityType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
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
  private final AuditLogService auditLogService;

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

    log.info("Organization {} created by user {}", organization.getId(), userId);

    auditLogService.record(
        organization.getId(),
        EntityType.ORGANIZATION,
        organization.getId(),
        ActionType.CREATE,
        "Created organization: " + organization.getName());

    return organizationMapper.toOrganizationResponse(organization);
  }

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

    List<String> changes = new ArrayList<>();
    if (!Objects.equals(organization.getName(), request.name())) {
      changes.add("name: '" + organization.getName() + "' -> '" + request.name() + "'");
    }
    if (!Objects.equals(organization.getDescription(), request.description())) {
      changes.add("description: '" + organization.getDescription() + "' -> '" + request.description() + "'");
    }
    if (!Objects.equals(organization.getAddress(), request.address())) {
      changes.add("address: '" + organization.getAddress() + "' -> '" + request.address() + "'");
    }
    if (!Objects.equals(organization.getHotline(), request.hotline())) {
      changes.add("hotline: '" + organization.getHotline() + "' -> '" + request.hotline() + "'");
    }
    if (!Objects.equals(organization.getTaxCode(), request.taxCode())) {
      changes.add("taxCode: '" + organization.getTaxCode() + "' -> '" + request.taxCode() + "'");
    }

    String message = "Updated organization: " + request.name();
    String changedField = null;
    String oldValue = null;
    String newValue = null;

    if (!changes.isEmpty()) {
      message += ". Changes: " + String.join(", ", changes);
      String firstChange = changes.get(0);
      int colonIdx = firstChange.indexOf(":");
      int arrowIdx = firstChange.indexOf(" -> ");
      if (colonIdx > 0 && arrowIdx > colonIdx) {
        changedField = firstChange.substring(0, colonIdx).trim();
        oldValue = firstChange.substring(colonIdx + 1, arrowIdx).replace("'", "").trim();
        newValue = firstChange.substring(arrowIdx + 4).replace("'", "").trim();
      }
    } else {
      message += ". No fields changed.";
    }

    organization.setName(request.name());
    organization.setDescription(request.description());
    organization.setAddress(request.address());
    organization.setHotline(request.hotline());
    organization.setTaxCode(request.taxCode());

    organization = organizationRepository.save(organization);
    log.info("Organization {} updated", organizationId);

    auditLogService.record(
        organizationId,
        EntityType.ORGANIZATION,
        organization.getId(),
        ActionType.UPDATE,
        changedField,
        oldValue,
        newValue,
        message);

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
