package com.dut.erp.controller.v1;

import com.dut.erp.dto.request.OrganizationInvitationUserRequest;
import com.dut.erp.security.CustomUserDetails;
import com.dut.erp.service.OrganizationInvitationService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/organizations/{organizationId}/invitations")
public class OrganizationInvitationController {
  private final OrganizationInvitationService organizationInvitationService;

  @PostMapping
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('organizations:manage', #organizationId, #userDetails)
      """)
  public ResponseEntity<Void> inviteUserToOrganization(
      @PathVariable UUID organizationId,
      @RequestBody @Valid OrganizationInvitationUserRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    organizationInvitationService.inviteUserToOrganization(
        organizationId, request.roleId(), request.email(), userDetails);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/{invitationId}/resend")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('organizations:manage', #organizationId, #userDetails)
      """)
  public ResponseEntity<Void> resendInvitationUserToOrganization(
      @PathVariable UUID organizationId,
      @PathVariable UUID invitationId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    organizationInvitationService.resendInvitationToOrganization(
        organizationId, invitationId, userDetails);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/{invitationId}")
  public ResponseEntity<Void> respondToOrganizationInvitation(
      @PathVariable UUID organizationId,
      @PathVariable UUID invitationId,
      @RequestParam boolean accept,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    organizationInvitationService.updateInvitationStatus(
        organizationId, invitationId, accept, userDetails);
    return ResponseEntity.ok().build();
  }
}
