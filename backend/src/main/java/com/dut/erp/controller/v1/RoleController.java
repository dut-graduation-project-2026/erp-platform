package com.dut.erp.controller.v1;

import com.dut.erp.dto.request.CreateRoleRequest;
import com.dut.erp.dto.response.RoleResponse;
import com.dut.erp.security.CustomUserDetails;
import com.dut.erp.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/v1/roles")
public class RoleController {
  private final RoleService roleService;

  @Operation(
      summary = "Get roles of the current user in the organization",
      description =
          "Returns all roles assigned to the currently authenticated user in the specified"
              + " organization.")
  @GetMapping("/me")
  public ResponseEntity<List<RoleResponse>> getRolesOfOrganizationOfCurrentUser(
      @RequestParam UUID organizationId, @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(
        roleService.getRolesByUserIdAndOrganizationId(userDetails.getId(), organizationId));
  }

  @Operation(
      summary = "Get all roles in the organization",
      description = "Returns all roles defined in the specified organization.")
  @GetMapping
  public ResponseEntity<List<RoleResponse>> getAllRolesOfOrganization(
      @RequestParam UUID organizationId, @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(roleService.getAllRolesByOrganizationId(organizationId));
  }

  @Operation(
      summary = "Get role details by role ID",
      description =
          "Returns the details of a specific role identified by its UUID within the specified"
              + " organization.")
  @GetMapping("/{roleId}")
  public ResponseEntity<RoleResponse> getRoleDetails(
      @PathVariable UUID roleId, @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(roleService.getRoleById(roleId));
  }

  @Operation(
      summary = "Create a new role in the organization",
      description =
          "Creates a new role with the specified name and permissions within the specified"
              + " organization.")
  @PostMapping
  public ResponseEntity<RoleResponse> createRole(
      @RequestBody CreateRoleRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    RoleResponse createdRole = roleService.addRole(request);
    return ResponseEntity.ok(createdRole);
  }
}
