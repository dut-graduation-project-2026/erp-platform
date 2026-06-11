package com.dut.erp.controller.v1;

import com.dut.erp.dto.request.CreateOrganizationRequest;
import com.dut.erp.dto.request.UpdateOrganizationRequest;
import com.dut.erp.dto.response.OrganizationResponse;
import com.dut.erp.security.CustomUserDetails;
import com.dut.erp.repository.PermissionRepository;
import com.dut.erp.service.OrganizationService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller handling organization management API endpoints.
 * Provides endpoints for creating, retrieving, and updating organizations.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/organizations")
public class OrganizationController {
  private final OrganizationService organizationService;
  private final PermissionRepository permissionRepository;


  /**
   * Creates a new organization and associates the authenticated creator with it.
   *
   * @param request the organization creation details
   * @param userDetails the authenticated user's details
   * @return a ResponseEntity containing the created OrganizationResponse object
   */
  @PostMapping
  public ResponseEntity<OrganizationResponse> createOrganization(
      @Valid @RequestBody CreateOrganizationRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    OrganizationResponse organizationResponse =
        organizationService.createOrganization(userDetails.getId(), request);
    return ResponseEntity.status(HttpStatus.CREATED).body(organizationResponse);
  }

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

  @GetMapping("/{organizationId}/my-permissions")
  @PreAuthorize("@securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)")
  public ResponseEntity<List<String>> getMyPermissions(
      @PathVariable UUID organizationId, @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(
        permissionRepository.findPermissionCodesByUserIdAndOrganizationId(
            userDetails.getId(), organizationId));
  }


  /**
   * Updates the details of a specific organization.
   *
   * <p>The user is resolved from the security context. The authenticated user must have
   * organization access and organizations:modify permission to update the organization.
   *
   * @param organizationId the UUID of the organization to update
   * @param request the update request containing organization details
   * @param userDetails the authenticated user's details (injected from security context)
   * @return a ResponseEntity containing the updated OrganizationResponse object
   * @throws AccessDeniedException if the authenticated user does not have required permissions
   * @throws ResourceNotFoundException if the organization does not exist
   */
  @PutMapping("/{organizationId}")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('organizations:write', #organizationId, #userDetails)
      """)
  public ResponseEntity<OrganizationResponse> updateOrganization(
      @PathVariable UUID organizationId,
      @Valid @RequestBody UpdateOrganizationRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    OrganizationResponse organizationResponse =
        organizationService.updateOrganization(organizationId, request);
    return ResponseEntity.ok(organizationResponse);
  }
}

