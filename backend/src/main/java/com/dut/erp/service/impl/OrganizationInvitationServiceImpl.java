package com.dut.erp.service.impl;

import com.dut.erp.constant.ExpirationDurationDefault;
import com.dut.erp.dto.event.OrganizationInvitationCreatedEvent;
import com.dut.erp.entity.Organization;
import com.dut.erp.entity.OrganizationInvitation;
import com.dut.erp.entity.Role;
import com.dut.erp.entity.User;
import com.dut.erp.enums.OrganizationInvitationStatus;
import com.dut.erp.exception.BadRequestException;
import com.dut.erp.exception.ResourceNotFoundException;
import com.dut.erp.repository.OrganizationInvitationRepository;
import com.dut.erp.repository.RoleRepository;
import com.dut.erp.repository.UserRepository;
import com.dut.erp.security.CustomUserDetails;
import com.dut.erp.service.OrganizationInvitationService;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganizationInvitationServiceImpl implements OrganizationInvitationService {

  private final OrganizationInvitationRepository organizationInvitationRepository;
  private final RoleRepository roleRepository;
  private final UserRepository userRepository;
  private final ApplicationEventPublisher applicationEventPublisher;

  @Override
  @Transactional
  public void inviteUserToOrganization(
      UUID organizationId, UUID roleId, String email, CustomUserDetails inviter) {

    Role role = findRoleByIdWithOrganization(roleId);
    Organization organization = role.getOrganization();

    if (!organizationId.equals(organization.getId())) {
      log.warn("Role {} does not belong to organization {}", roleId, organizationId);
      throw new BadRequestException("Role does not belong to the specified organization.");
    }

    if (isUserAlreadyInvited(email, organizationId)) {
      log.warn("Email {} is already invited to organization {}", email, organizationId);
      throw new BadRequestException("User already has a pending invitation to this organization.");
    }

    userRepository
        .findByEmail(email)
        .ifPresent(
            existingUser -> {
              boolean isMember =
                  existingUser.getOrganizations().stream()
                      .anyMatch(org -> org.getId().equals(organizationId));
              boolean hasRole =
                  existingUser.getRoles().stream().anyMatch(r -> r.getId().equals(roleId));

              if (isMember && hasRole) {
                log.warn(
                    "User {} already has role {} in organization {}",
                    email,
                    roleId,
                    organizationId);
                throw new BadRequestException("User already has this role in the organization.");
              }
            });

    OrganizationInvitation invitation =
        OrganizationInvitation.builder()
            .organization(organization)
            .role(role)
            .email(email)
            .expiresAt(
                Instant.now()
                    .plusMillis(ExpirationDurationDefault.INVITATION_EXPIRATION_DURATION_MS))
            .build();

    organizationInvitationRepository.save(invitation);

    applicationEventPublisher.publishEvent(
        new OrganizationInvitationCreatedEvent(invitation.getId()));

    log.info("User {} invited to organization {} by {}", email, organizationId, inviter.getEmail());
  }

  @Override
  @Transactional
  public void resendInvitationToOrganization(UUID invitationId, CustomUserDetails inviter) {

    OrganizationInvitation invitation = getInvitationById(invitationId);

    if (invitation.getStatus() != OrganizationInvitationStatus.PENDING) {
      log.warn("Cannot resend invitation {} with status {}", invitationId, invitation.getStatus());
      throw new BadRequestException(
          "Only pending invitations can be resent. Current status: "
              + invitation.getStatus()
              + ".");
    }

    if (Instant.now().toEpochMilli() - invitation.getUpdatedAt().toEpochMilli()
        < ExpirationDurationDefault.RESEND_INVITATION_INTERVAL_MS) {
      log.warn("Invitation {} was resent too recently", invitationId);
      throw new BadRequestException("You can only resend an invitation every 2 minutes.");
    }

    invitation.setExpiresAt(
        Instant.now().plusMillis(ExpirationDurationDefault.INVITATION_EXPIRATION_DURATION_MS));

    organizationInvitationRepository.save(invitation);

    applicationEventPublisher.publishEvent(new OrganizationInvitationCreatedEvent(invitationId));

    log.info(
        "Invitation {} resent to {} by {}",
        invitationId,
        invitation.getEmail(),
        inviter.getEmail());
  }

  @Override
  @Transactional
  public void updateInvitationStatus(
      UUID invitationId, boolean accepted, CustomUserDetails responder) {

    OrganizationInvitation invitation = getInvitationById(invitationId);

    if (!invitation.getEmail().equalsIgnoreCase(responder.getEmail())) {
      throw new AccessDeniedException("You are not authorized to respond to this invitation");
    }

    if (invitation.isExpired()) {
      log.warn("Invitation with ID {} has expired", invitationId);
      throw new BadRequestException("Invitation has expired.");
    }

    if (invitation.getStatus() != OrganizationInvitationStatus.PENDING) {
      log.warn(
          "Invitation with ID {} is not pending. Current status: {}",
          invitationId,
          invitation.getStatus());
      throw new BadRequestException(
          "Invitation is no longer pending. Current status: " + invitation.getStatus() + ".");
    }

    User responderUser = findUserByIdWithRolesAndOrganizations(responder.getId());

    invitation.setStatus(
        accepted ? OrganizationInvitationStatus.ACCEPTED : OrganizationInvitationStatus.DECLINED);

    if (accepted) {
      addUserToOrganization(invitation, responderUser);
    }

    invitation.setAcceptedBy(responderUser);
    organizationInvitationRepository.save(invitation);
  }

  @Override
  public OrganizationInvitation getInvitationById(UUID invitationId) {
    return organizationInvitationRepository
        .findByIdWithContext(invitationId)
        .orElseThrow(
            () -> {
              log.warn("Invitation with ID {} not found", invitationId);
              return new ResourceNotFoundException("Invitation not found with id: " + invitationId);
            });
  }

  private Role findRoleByIdWithOrganization(UUID roleId) {
    return roleRepository
        .findByIdWithOrganization(roleId)
        .orElseThrow(
            () -> {
              log.warn("Role with ID {} not found", roleId);
              return new ResourceNotFoundException("Role not found with id: " + roleId);
            });
  }

  private User findUserByIdWithRolesAndOrganizations(UUID userId) {
    return userRepository
        .findByIdWithRolesAndOrganizations(userId)
        .orElseThrow(
            () -> {
              log.warn("User with ID {} not found", userId);
              return new ResourceNotFoundException("User not found with id: " + userId);
            });
  }

  private void addUserToOrganization(OrganizationInvitation invitation, User user) {
    Role role = invitation.getRole();

    if (role == null) {
      log.warn("Invitation {} has no role assigned", invitation.getId());
      throw new BadRequestException("Invitation does not have a role assigned.");
    }

    UUID organizationId = invitation.getOrganization().getId();
    if (!organizationId.equals(role.getOrganization().getId())) {
      log.warn("Role {} does not belong to organization {}", role.getId(), organizationId);
      throw new BadRequestException("Invitation role does not belong to the organization.");
    }

    boolean alreadyMember =
        user.getOrganizations().stream().anyMatch(org -> org.getId().equals(organizationId));

    if (alreadyMember) {
      boolean alreadyHasRole =
          user.getRoles().stream().anyMatch(r -> r.getId().equals(role.getId()));
      if (alreadyHasRole) {
        log.warn(
            "User {} already has role {} in organization {}",
            user.getId(),
            role.getId(),
            organizationId);
        throw new BadRequestException("User already has this role in the organization.");
      }
      user.getRoles().add(role);
      log.info(
          "Added role {} to existing member {} in organization {}",
          role.getId(),
          user.getId(),
          organizationId);
    } else {
      user.getOrganizations().add(invitation.getOrganization());
      user.getRoles().add(role);
      log.info(
          "Added user {} to organization {} with role {}",
          user.getId(),
          organizationId,
          role.getId());
    }

    userRepository.save(user);
  }

  private boolean isUserAlreadyInvited(String email, UUID organizationId) {
    return organizationInvitationRepository.existsByEmailAndOrganizationIdAndStatus(
        email, organizationId, OrganizationInvitationStatus.PENDING);
  }
}
