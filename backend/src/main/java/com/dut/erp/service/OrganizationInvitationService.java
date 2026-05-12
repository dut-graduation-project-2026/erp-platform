package com.dut.erp.service;

import com.dut.erp.entity.OrganizationInvitation;
import com.dut.erp.security.CustomUserDetails;
import java.util.UUID;

public interface OrganizationInvitationService {
  void inviteUserToOrganization(
      UUID organizationId, UUID roleId, String email, CustomUserDetails inviter);

  void resendInvitationToOrganization(
      UUID invitationId, CustomUserDetails inviter);

  void updateInvitationStatus(UUID invitationId, boolean accepted, CustomUserDetails responder);

  OrganizationInvitation getInvitationById(UUID invitationId);
}
