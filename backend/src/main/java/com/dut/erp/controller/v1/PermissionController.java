package com.dut.erp.controller.v1;

import com.dut.erp.dto.response.PermissionResponse;
import com.dut.erp.security.CustomUserDetails;
import com.dut.erp.service.PermissionService;
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

  @GetMapping("/me")
  public ResponseEntity<List<PermissionResponse>> getCurrentUserPermissionsOfOrganization(
      @RequestParam UUID organizationId, @AuthenticationPrincipal CustomUserDetails userDetails) {
    List<PermissionResponse> permissionResponses =
        permissionService.getPermissionsByOrganizationIdAndUserId(organizationId, userDetails.getId());
    return ResponseEntity.ok(permissionResponses);
  }
}
