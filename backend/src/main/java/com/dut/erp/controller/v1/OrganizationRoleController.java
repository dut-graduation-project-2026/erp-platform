package com.dut.erp.controller.v1;

import com.dut.erp.dto.request.CreateRoleRequest;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.UpdateRoleRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.RoleBaseResponse;
import com.dut.erp.dto.response.RoleResponse;
import com.dut.erp.security.CustomUserDetails;
import com.dut.erp.service.RoleService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller handling organization roles and permissions.
 * Provides CRUD endpoints for managing user roles within a specific organization.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/organizations/{organizationId}/roles")
public class OrganizationRoleController {
  private final RoleService roleService;

  /**
   * Creates a new role within the specified organization.
   *
   * @param organizationId the UUID of the organization
   * @param request the create role request containing the name, description, and permissions
   * @param userDetails the authenticated user's details
   * @return a ResponseEntity containing the created RoleResponse object
   */
  @PostMapping
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('roles:create', #organizationId, #userDetails)
      """)
  public ResponseEntity<RoleResponse> createRole(
      @PathVariable UUID organizationId,
      @Valid @RequestBody CreateRoleRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(roleService.createRole(organizationId, request));
  }

  /**
   * Retrieves a paginated list of roles belonging to the specified organization.
   *
   * @param organizationId the UUID of the organization
   * @param paginationRequest the pagination parameters (page and limit)
   * @param userDetails the authenticated user's details
   * @return a ResponseEntity containing a paged response of RoleBaseResponse objects
   */
  @GetMapping
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('roles:read', #organizationId, #userDetails)
      """)
  public ResponseEntity<PagedEntityResponse<RoleBaseResponse>> getRolesOfOrganization(
      @PathVariable UUID organizationId,
      @Valid @ModelAttribute PaginationRequest paginationRequest,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(
        roleService.getRolesByOrganizationId(organizationId, paginationRequest));
  }

  /**
   * Retrieves details of a specific role by its ID within the specified organization.
   *
   * @param id the UUID of the role
   * @param organizationId the UUID of the organization
   * @param userDetails the authenticated user's details
   * @return a ResponseEntity containing the RoleResponse object
   */
  @GetMapping("/{id}")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('roles:select', #organizationId, #userDetails)
        and
        @roleService.isRoleBelongsToOrganization(#id, #organizationId)
      """)
  public ResponseEntity<RoleResponse> getRoleById(
      @PathVariable UUID id,
      @PathVariable UUID organizationId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(roleService.getRoleByIdWithOrganizationAndPermissionAndModule(id));
  }

  /**
   * Updates a specific role in the organization.
   *
   * <p>The user is resolved from the security context. The authenticated user must have
   * organization access and roles:modify permission to update the role.
   *
   * @param organizationId the UUID of the organization
   * @param id the UUID of the role to update
   * @param request the update request containing role details
   * @param userDetails the authenticated user's details (injected from security context)
   * @return a ResponseEntity containing the updated RoleResponse object
   * @throws AccessDeniedException if the authenticated user does not have required permissions
   * @throws ResourceNotFoundException if the organization or role does not exist
   * @throws BadRequestException if the role does not belong to the specified organization
   */
  @PutMapping("/{id}")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('roles:write', #organizationId, #userDetails)
        and
        @roleService.isRoleBelongsToOrganization(#id, #organizationId)
      """)
  public ResponseEntity<RoleResponse> updateRole(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @Valid @RequestBody UpdateRoleRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(roleService.updateRole(id, organizationId, request));
  }

  /**
   * Deletes a specific role from the organization.
   *
   * <p>The user is resolved from the security context. The authenticated user must have
   * organization access and roles:delete permission to delete the role.
   *
   * @param organizationId the UUID of the organization
   * @param id the UUID of the role to delete
   * @param userDetails the authenticated user's details (injected from security context)
   * @return a ResponseEntity with no content status (204)
   * @throws AccessDeniedException if the authenticated user does not have required permissions
   * @throws ResourceNotFoundException if the organization or role does not exist
   * @throws BadRequestException if the role does not belong to the specified organization
   */
  @DeleteMapping("/{id}")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('roles:delete', #organizationId, #userDetails)
      """)
  public ResponseEntity<Void> deleteRole(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    roleService.deleteRole(id, organizationId);
    return ResponseEntity.noContent().build();
  }
}
