package com.dut.erp.controller.v1;

import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.UpdateUserRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.UserBaseResponse;
import com.dut.erp.security.CustomUserDetails;
import com.dut.erp.service.UserService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import com.dut.erp.dto.request.UpdateUserRolesRequest;
import com.dut.erp.dto.response.OrganizationMemberResponse;
import com.dut.erp.dto.request.ChangePasswordRequest;

/**
 * Controller handling user-related API endpoints.
 * Provides endpoints for retrieving and updating user profile information.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {
  private final UserService userService;

  /**
   * Retrieves a paginated list of users belonging to a specific organization,
   * optionally filtered by email or name.
   *
   * @param organizationId the UUID of the organization
   * @param query the optional search query (email or name filter)
   * @param paginationRequest the pagination parameters (page and limit)
   * @param userDetails the authenticated user's details
   * @return a ResponseEntity containing a paged response of UserBaseResponse objects
   */
  @GetMapping
  @PreAuthorize(
      """
      @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
      and
      @securityAuthService.hasPermission('users:read', #organizationId, #userDetails)
      """)
  public ResponseEntity<PagedEntityResponse<OrganizationMemberResponse>> getUsersOfOrganization(
      @RequestParam UUID organizationId,
      @RequestParam(required = false) String query,
      @Valid @ModelAttribute PaginationRequest paginationRequest,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    PagedEntityResponse<OrganizationMemberResponse> response =
        userService.searchUsersByOrganizationId(organizationId, query, paginationRequest);
    return ResponseEntity.ok(response);
  }

  /**
   * Updates the user information.
   *
   * <p>The user is resolved from the security context. The authenticated user can only update their
   * own profile information.
   *
   * @param userId the UUID of the user to update
   * @param request the update request containing user details
   * @param userDetails the authenticated user's details (injected from security context)
   * @return a ResponseEntity containing the updated UserBaseResponse object
   * @throws AccessDeniedException if the authenticated user is not the target user
   * @throws ResourceNotFoundException if the user does not exist
   */
  @PutMapping("/{userId}")
  @PreAuthorize("#userId == #userDetails.id")
  public ResponseEntity<UserBaseResponse> updateUser(
      @PathVariable UUID userId,
      @Valid @RequestBody UpdateUserRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    UserBaseResponse userResponse = userService.updateUser(userId, request);
    return ResponseEntity.ok(userResponse);
  }

  /**
   * Retrieves detail of a user belonging to a specific organization.
   *
   * @param organizationId the UUID of the organization
   * @param userId the UUID of the user
   * @param userDetails the authenticated user's details
   * @return a ResponseEntity containing an OrganizationMemberResponse object
   */
  @GetMapping("/{userId}")
  @PreAuthorize(
      """
      @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
      and
      @securityAuthService.hasPermission('users:read', #organizationId, #userDetails)
      """)
  public ResponseEntity<OrganizationMemberResponse> getUserById(
      @RequestParam UUID organizationId,
      @PathVariable UUID userId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    OrganizationMemberResponse response = userService.getUserByIdAndOrganizationId(userId, organizationId);
    return ResponseEntity.ok(response);
  }

  /**
   * Updates the roles of a user within a specific organization.
   *
   * @param userId the UUID of the user
   * @param request the update request containing organization ID and list of role IDs
   * @param userDetails the authenticated user's details
   * @return a ResponseEntity containing the updated OrganizationMemberResponse object
   */
  @PutMapping("/{userId}/roles")
  @PreAuthorize(
      """
      @securityAuthService.hasOrganizationAccess(#request.organizationId(), #userDetails)
      and
      @securityAuthService.hasPermission('users:write', #request.organizationId(), #userDetails)
      """)
  public ResponseEntity<OrganizationMemberResponse> updateUserRoles(
      @PathVariable UUID userId,
      @Valid @RequestBody UpdateUserRolesRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    OrganizationMemberResponse response = userService.updateUserRoles(userId, request.organizationId(), request.roleIds());
    return ResponseEntity.ok(response);
  }

  /**
   * Removes a user from a specific organization.
   *
   * @param userId the UUID of the user to remove
   * @param organizationId the UUID of the organization
   * @param userDetails the authenticated user's details
   * @return a ResponseEntity with no content (204)
   */
  @DeleteMapping("/{userId}")
  @PreAuthorize(
      """
      @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
      and
      @securityAuthService.hasPermission('users:delete', #organizationId, #userDetails)
      """)
  public ResponseEntity<Void> removeUserFromOrganization(
      @PathVariable UUID userId,
      @RequestParam UUID organizationId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    userService.removeUserFromOrganization(userId, organizationId);
    return ResponseEntity.noContent().build();
  }

  /**
   * Updates the password of the authenticated user.
   *
   * @param userId the UUID of the user
   * @param request the request containing old and new passwords
   * @param userDetails the authenticated user's details
   * @return a ResponseEntity with no content (204)
   */
  @PutMapping("/{userId}/change-password")
  @PreAuthorize("#userId == #userDetails.id")
  public ResponseEntity<Void> changePassword(
      @PathVariable UUID userId,
      @Valid @RequestBody ChangePasswordRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    userService.changePassword(userId, request);
    return ResponseEntity.noContent().build();
  }
}
