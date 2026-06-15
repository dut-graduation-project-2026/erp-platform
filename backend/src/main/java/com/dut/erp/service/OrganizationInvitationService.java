package com.dut.erp.service;

import com.dut.erp.dto.response.OrganizationInvitationResponse;
import com.dut.erp.entity.OrganizationInvitation;
import com.dut.erp.security.CustomUserDetails;
import java.util.UUID;
import java.util.List;

public interface OrganizationInvitationService {
  OrganizationInvitationResponse inviteUserToOrganization(
      UUID organizationId, UUID roleId, String email, CustomUserDetails inviter);

  List<OrganizationInvitationResponse> bulkInviteUsersToOrganization(
      UUID organizationId, UUID roleId, List<String> emails, CustomUserDetails inviter);

  OrganizationInvitationResponse resendInvitationToOrganization(
      UUID organizationId, UUID invitationId, CustomUserDetails inviter);

  OrganizationInvitationResponse updateInvitationStatus(
      UUID organizationId, UUID invitationId, boolean accepted, CustomUserDetails responder);

  OrganizationInvitation getInvitationById(UUID invitationId);

  java.util.List<OrganizationInvitationResponse> getInvitationsByOrganizationId(UUID organizationId);
}
