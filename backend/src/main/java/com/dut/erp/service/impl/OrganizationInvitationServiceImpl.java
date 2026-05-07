package com.dut.erp.service.impl;

import com.dut.erp.constant.ExpirationDurationDefault;
import com.dut.erp.dto.event.OrganizationInvitationCreatedEvent;
import com.dut.erp.entity.Organization;
import com.dut.erp.entity.OrganizationInvitation;
import com.dut.erp.enums.OrganizationInvitationStatus;
import com.dut.erp.exception.BadRequestException;
import com.dut.erp.exception.ResourceNotFoundException;
import com.dut.erp.repository.OrganizationInvitationRepository;
import com.dut.erp.security.CustomUserDetails;
import com.dut.erp.service.OrganizationInvitationService;
import com.dut.erp.service.OrganizationService;
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
  private final OrganizationService organizationService;
  private final ApplicationEventPublisher applicationEventPublisher;

  @Override
  @Transactional
  public void inviteUserToOrganization(
      UUID organizationId, String email, CustomUserDetails inviter) {

    Organization organization = organizationService.findOrganizationById(organizationId);

    if (isUserAlreadyInvited(email, organizationId)) {
      log.warn("Email {} is already invited to organization {}", email, organizationId);

      throw new BadRequestException("User already has a pending invitation to this organization");
    }

    OrganizationInvitation invitation =
        OrganizationInvitation.builder()
            .organization(organization)
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
  public void updateInvitationStatus(
      UUID invitationId, boolean accepted, CustomUserDetails responder) {

    OrganizationInvitation invitation = getInvitationById(invitationId);

    if (!invitation.getEmail().equalsIgnoreCase(responder.getEmail())) {
      throw new AccessDeniedException("You are not authorized to respond to this invitation");
    }

    if (!invitation.isPending()) {
      log.warn(
          "Invitation with ID {} is not pending. Current status: {}",
          invitationId,
          invitation.getStatus());

      throw new BadRequestException(
          "Invitation is no longer pending. Current status: " + invitation.getStatus());
    }

    if (invitation.isExpired()) {
      log.warn("Invitation with ID {} has expired", invitationId);

      throw new BadRequestException("Invitation has expired");
    }

    invitation.setStatus(
        accepted ? OrganizationInvitationStatus.ACCEPTED : OrganizationInvitationStatus.DECLINED);

    if (accepted) {
      organizationService.addMemberToOrganization(
          invitation.getOrganization().getId(), responder.getId());
    }

    organizationInvitationRepository.save(invitation);
  }

  @Override
  public OrganizationInvitation getInvitationById(UUID invitationId) {
    return organizationInvitationRepository
        .findById(invitationId)
        .orElseThrow(
            () -> {
              log.warn("Invitation with ID {} not found", invitationId);

              return new ResourceNotFoundException("Invitation not found");
            });
  }

  private boolean isUserAlreadyInvited(String email, UUID organizationId) {

    return organizationInvitationRepository.existsByEmailAndOrganizationIdAndStatus(
        email, organizationId, OrganizationInvitationStatus.PENDING);
  }
}
