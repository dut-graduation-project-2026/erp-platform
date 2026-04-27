package com.dut.erp.controller.v1;

import com.dut.erp.dto.response.PermissionResponse;
import com.dut.erp.security.CustomUserDetails;
import com.dut.erp.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/permissions")
public class PermissionController {

  private final PermissionService permissionService;

  /**
   * Retrieves all permissions defined in the given organization.
   *
   * <p>Returns the full permission catalog of the organization, including all available actions
   * visible to the caller.
   *
   * <p>Requires: caller must be restricted to admins or managers via additional authorization.
   *
   * @param organizationId the UUID of the target organization
   * @param userDetails the authenticated user's details (injected from security context)
   * @return {@code 200 OK} with a list of {@link PermissionResponse}
   */
  @Operation(
      summary = "Get organization permission catalog",
      description = "Returns all permissions defined in the organization.")
  @GetMapping
  public ResponseEntity<List<PermissionResponse>> getOrganizationPermissions(
      @Parameter(description = "UUID of the organization", required = true) @RequestParam
          UUID organizationId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(
        permissionService.getAllPermissionsOfOrganizationWithActionsByOrganizationId(
            organizationId));
  }

  /**
   * Retrieves all permissions granted to a specific user in the given organization.
   *
   * <p>Requires: caller must be restricted to admins or managers via additional authorization.
   *
   * @param organizationId the UUID of the target organization
   * @param userId the UUID of the user whose granted permissions are queried
   * @param userDetails the authenticated user's details (injected from security context)
   * @return {@code 200 OK} with a list of {@link PermissionResponse}
   */
  @Operation(
      summary = "Get granted permissions of a user",
      description = "Returns all permissions granted to the specified user in the organization.")
  @GetMapping("/granted")
  public ResponseEntity<List<PermissionResponse>> getUserGrantedPermissions(
      @Parameter(description = "UUID of the organization", required = true) @RequestParam
          UUID organizationId,
      @Parameter(description = "UUID of the target user", required = true) @RequestParam
          UUID userId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(
        permissionService.getAllPermissionsOfUserWithActionsByUserIdAndOrganizationId(
            userId, organizationId));
  }

  /**
   * Retrieves all permissions granted to the current authenticated user in the given organization.
   *
   * <p>Resolves the user from the security context — no userId param required.
   *
   * <p>Requires: caller must be a member of the organization.
   *
   * @param organizationId the UUID of the target organization
   * @param userDetails the authenticated user's details (injected from security context)
   * @return {@code 200 OK} with a list of {@link PermissionResponse}
   */
  @Operation(
      summary = "Get my granted permissions",
      description = "Returns all permissions granted to the currently authenticated user.")
  @GetMapping("/granted/me")
  public ResponseEntity<List<PermissionResponse>> getMyGrantedPermissions(
      @Parameter(description = "UUID of the organization", required = true) @RequestParam
          UUID organizationId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(
        permissionService.getAllPermissionsOfUserWithActionsByUserIdAndOrganizationId(
            userDetails.getId(), organizationId));
  }
}
