package com.dut.erp.controller.v1;

import com.dut.erp.dto.request.OrganizationInvitationUserRequest;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.response.OrganizationResponse;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.UserBaseResponse;
import com.dut.erp.security.CustomUserDetails;
import com.dut.erp.service.OrganizationInvitationService;
import com.dut.erp.service.OrganizationService;
import com.dut.erp.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/organizations")
public class OrganizationController {
  private final OrganizationService organizationService;
  private final UserService userService;
  private final OrganizationInvitationService organizationInvitationService;

  /**
   * Retrieves all organizations that the current authenticated user belongs to.
   *
   * <p>The user is resolved from the security context. Returns an empty list if the user is not a
   * member of any organization.
   *
   * @param userDetails the authenticated user's details (injected from security context)
   * @return a ResponseEntity containing a list of OrganizationResponse objects
   */
  @GetMapping("/me")
  public ResponseEntity<List<OrganizationResponse>> getOrganizationsOfCurrentUser(
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    List<OrganizationResponse> organizationResponses =
        organizationService.getOrganizationsByUserId(userDetails.getId());
    return ResponseEntity.ok(organizationResponses);
  }

  /**
   * Retrieves the details of a specific organization by its ID.
   *
   * <p>The user is resolved from the security context. The authenticated user must have access to
   * the organization to retrieve its details.
   *
   * @param organizationId the UUID of the organization to retrieve
   * @param userDetails the authenticated user's details (injected from security context)
   * @return a ResponseEntity containing the OrganizationResponse object for the specified
   *     organization ID
   * @throws AccessDeniedException if the authenticated user does not have access to the
   *     organization
   */
  @GetMapping("/{organizationId}")
  @PreAuthorize("@securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)")
  public ResponseEntity<OrganizationResponse> getOrganizationById(
      @PathVariable UUID organizationId, @AuthenticationPrincipal CustomUserDetails userDetails) {
    OrganizationResponse organizationResponse =
        organizationService.getOrganizationById(organizationId);
    return ResponseEntity.ok(organizationResponse);
  }

  @GetMapping("/{organizationId}/users")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('organizations:manage', #organizationId, #userDetails)
      """)
  public ResponseEntity<PagedEntityResponse<UserBaseResponse>> getUserOfOrganization(
      @PathVariable UUID organizationId,
      @Valid @ModelAttribute PaginationRequest paginationRequest,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    PagedEntityResponse<UserBaseResponse> response =
        userService.getUsersByOrganizationId(organizationId, paginationRequest);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/{organizationId}/invitations")
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
        organizationId, request.email(), userDetails);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/{organizationId}/invitations/{invitationId}")
  // @PreAuthorize("@securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)")
  public ResponseEntity<Void> respondToOrganizationInvitation(
      @PathVariable UUID organizationId,
      @PathVariable UUID invitationId,
      @RequestParam boolean accept,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    organizationInvitationService.updateInvitationStatus(invitationId, accept, userDetails);
    return ResponseEntity.ok().build();
  }
}
