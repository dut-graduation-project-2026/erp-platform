package com.dut.erp.service.impl;

import com.dut.erp.dto.response.OrganizationResponse;
import com.dut.erp.entity.Organization;
import com.dut.erp.entity.User;
import com.dut.erp.exception.ResourceNotFoundException;
import com.dut.erp.mapper.OrganizationMapper;
import com.dut.erp.repository.OrganizationRepository;
import com.dut.erp.service.OrganizationService;
import com.dut.erp.service.UserService;
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

  private final UserService userService;

  @Override
  public List<OrganizationResponse> getOrganizationsByUserId(UUID userId) {
    return organizationRepository.findAllWithUserId(userId).stream()
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
              return new ResourceNotFoundException("Organization not found");
            });
  }

  @Override
  @Transactional
  public Organization addMemberToOrganization(UUID organizationId, UUID userId) {
    Organization organization =
        organizationRepository
            .findByIdWithUsers(organizationId)
            .orElseThrow(
                () -> {
                  log.warn("Organization with ID {} not found", organizationId);
                  return new ResourceNotFoundException("Organization not found");
                });

    User user = userService.findUserById(userId);

    boolean alreadyMember =
        organization.getUsers().stream().anyMatch(u -> u.getId().equals(user.getId()));
    if (alreadyMember) {
      log.warn("User {} is already a member of organization {}", userId, organizationId);
      throw new IllegalStateException("User is already a member of this organization");
    }

    organization.getUsers().add(user);
    return organizationRepository.save(organization);
  }
}
