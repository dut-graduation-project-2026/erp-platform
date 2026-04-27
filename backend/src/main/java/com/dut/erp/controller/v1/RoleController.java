package com.dut.erp.controller.v1;

import com.dut.erp.dto.response.RoleResponse;
import com.dut.erp.security.CustomUserDetails;
import com.dut.erp.service.RoleService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/roles")
public class RoleController {
  private final RoleService roleService;

  @GetMapping("/me")
  @PreAuthorize("@securityService.isMemberOfOrganization(#userDetails.id, #organizationId)")
  public ResponseEntity<List<RoleResponse>> getRolesOfOrganizationOfCurrentUser(
      @RequestParam UUID organizationId, @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(
        roleService.getRolesByUserIdAndOrganizationId(userDetails.getId(), organizationId));
  }
}
