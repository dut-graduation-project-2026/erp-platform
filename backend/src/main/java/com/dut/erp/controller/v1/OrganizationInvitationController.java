package com.dut.erp.controller.v1;

import com.dut.erp.dto.request.OrganizationInvitationUserRequest;
import com.dut.erp.dto.request.UpdateOrganizationInvitationStatusRequest;
import com.dut.erp.dto.response.OrganizationInvitationResponse;
import com.dut.erp.security.CustomUserDetails;
import com.dut.erp.service.OrganizationInvitationService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * Controller handling organization invitation endpoints.
 * Provides endpoints to invite users, resend invitations, and respond to invitations.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/organizations/{organizationId}/invitations")
public class OrganizationInvitationController {
  private final OrganizationInvitationService organizationInvitationService;

  /**
   * Invites a user to the specified organization.
   *
   * @param organizationId the UUID of the organization
   * @param request the invitation request containing the role ID and user email
   * @param userDetails the authenticated user's details
   * @return a ResponseEntity containing the created OrganizationInvitationResponse
   */
  @PostMapping
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('organizations:write', #organizationId, #userDetails)
      """)
  public ResponseEntity<OrganizationInvitationResponse> inviteUserToOrganization(
      @PathVariable UUID organizationId,
      @RequestBody @Valid OrganizationInvitationUserRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    OrganizationInvitationResponse response =
        organizationInvitationService.inviteUserToOrganization(
            organizationId, request.roleId(), request.email(), userDetails);
    return ResponseEntity.ok(response);
  }

  /**
   * Bulk invites users to the specified organization.
   *
   * @param organizationId the UUID of the organization
   * @param request the bulk invitation request containing the role ID and user emails
   * @param userDetails the authenticated user's details
   * @return a ResponseEntity containing the created OrganizationInvitationResponses
   */
  @PostMapping("/bulk")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('organizations:write', #organizationId, #userDetails)
      """)
  public ResponseEntity<List<OrganizationInvitationResponse>> bulkInviteUsersToOrganization(
      @PathVariable UUID organizationId,
      @RequestBody @Valid com.dut.erp.dto.request.BulkOrganizationInvitationRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    List<OrganizationInvitationResponse> response =
        organizationInvitationService.bulkInviteUsersToOrganization(
            organizationId, request.roleId(), request.emails(), userDetails);
    return ResponseEntity.ok(response);
  }

  /**
   * Resends an existing organization invitation.
   *
   * @param organizationId the UUID of the organization
   * @param invitationId the UUID of the invitation to resend
   * @param userDetails the authenticated user's details
   * @return a ResponseEntity containing the resent OrganizationInvitationResponse
   */
  @PostMapping("/{invitationId}/resend")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('organizations:write', #organizationId, #userDetails)
      """)
  public ResponseEntity<OrganizationInvitationResponse> resendInvitationUserToOrganization(
      @PathVariable UUID organizationId,
      @PathVariable UUID invitationId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    OrganizationInvitationResponse response =
        organizationInvitationService.resendInvitationToOrganization(
            organizationId, invitationId, userDetails);
    return ResponseEntity.ok(response);
  }

  /**
   * Responds to an organization invitation (accept or reject).
   *
   * @param organizationId the UUID of the organization
   * @param invitationId the UUID of the invitation
   * @param request the request containing the acceptance status
   * @param userDetails the authenticated user's details
   * @return a ResponseEntity containing the updated OrganizationInvitationResponse
   */
  @PatchMapping("/{invitationId}")
  public ResponseEntity<OrganizationInvitationResponse> respondToOrganizationInvitation(
      @PathVariable UUID organizationId,
      @PathVariable UUID invitationId,
      @RequestBody @Valid UpdateOrganizationInvitationStatusRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    OrganizationInvitationResponse response =
        organizationInvitationService.updateInvitationStatus(
            organizationId, invitationId, request.accepted(), userDetails);
    return ResponseEntity.ok(response);
  }

  /**
   * Retrieves all invitations for the specified organization.
   *
   * @param organizationId the UUID of the organization
   * @param userDetails the authenticated user's details
   * @return a ResponseEntity containing a list of OrganizationInvitationResponse
   */
  @GetMapping
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('organizations:read', #organizationId, #userDetails)
      """)
  public ResponseEntity<List<OrganizationInvitationResponse>> getInvitationsOfOrganization(
      @PathVariable UUID organizationId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    List<OrganizationInvitationResponse> response =
        organizationInvitationService.getInvitationsByOrganizationId(organizationId);
    return ResponseEntity.ok(response);
  }
}
