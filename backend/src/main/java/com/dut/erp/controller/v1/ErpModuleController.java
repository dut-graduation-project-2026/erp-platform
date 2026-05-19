package com.dut.erp.controller.v1;

import com.dut.erp.dto.response.ErpModuleBaseResponse;
import com.dut.erp.dto.response.ErpModuleResponse;
import com.dut.erp.security.CustomUserDetails;
import com.dut.erp.service.ErpModuleService;
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

/**
 * Controller handling ERP module management and retrieval API endpoints.
 * Provides endpoints for retrieving accessible modules for a user and organization.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/erp-modules")
public class ErpModuleController {
  private final ErpModuleService erpModuleService;

  /**
   * Retrieves the list of ERP modules that the current authenticated user has access to within a
   * specific organization.
   *
   * <p>The user is resolved from the security context, and the organization ID is provided as a
   * request parameter. The method checks if the user has access to the specified organization
   * before fetching the modules. If the user does not have access, a 403 Forbidden response will be
   * returned.
   *
   * @param organizationId the ID of the organization to filter ERP modules by
   * @param userDetails the authenticated user's details (injected from security context)
   * @return a ResponseEntity containing a list of ErpModuleBaseResponse objects that the user has
   *     access to
   */
  @GetMapping("/me")
  @PreAuthorize("@securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)")
  public ResponseEntity<List<ErpModuleBaseResponse>> getMyErpModulesByOrganization(
      @RequestParam UUID organizationId, @AuthenticationPrincipal CustomUserDetails userDetails) {
    List<ErpModuleBaseResponse> modules =
        erpModuleService.getErpModulesByUserAndOrganization(userDetails.getId(), organizationId);
    return ResponseEntity.ok(modules);
  }

  /**
   * Retrieves all ERP modules available in the specified organization.
   *
   * <p>This endpoint requires organization access and the {@code erp_module:read} permission.
   *
   * @param organizationId the ID of the organization
   * @param userDetails the authenticated user's details (injected from security context)
   * @return a ResponseEntity containing the list of ERP modules in the organization
   */
  @GetMapping
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('erp_module:read', #organizationId, #userDetails)
      """)
  public ResponseEntity<List<ErpModuleResponse>> getErpModulesByOrganization(
      @RequestParam UUID organizationId, @AuthenticationPrincipal CustomUserDetails userDetails) {
    List<ErpModuleResponse> modules = erpModuleService.getErpModulesByOrganization(organizationId);
    return ResponseEntity.ok(modules);
  }
}
