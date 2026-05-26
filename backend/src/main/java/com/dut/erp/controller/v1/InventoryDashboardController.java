package com.dut.erp.controller.v1;

import com.dut.erp.dto.response.InventoryDashboardResponse;
import com.dut.erp.security.CustomUserDetails;
import com.dut.erp.service.InventoryDashboardService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for inventory and warehouse analytics within an organization.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/organizations/{organizationId}/inventory-dashboard")
public class InventoryDashboardController {

  private final InventoryDashboardService inventoryDashboardService;

  /**
   * Returns inventory analytics KPIs: total valuation, turnover ratio, and aging breakdown.
   */
  @GetMapping
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and (
          @securityAuthService.hasPermission('quants:select', #organizationId, #userDetails)
          or
          @securityAuthService.hasPermission('warehouses:select', #organizationId, #userDetails)
          or
          @securityAuthService.hasPermission('inventory:select', #organizationId, #userDetails)
        )
      """)
  public ResponseEntity<InventoryDashboardResponse> getInventoryDashboard(
      @PathVariable UUID organizationId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(inventoryDashboardService.getDashboard(organizationId));
  }
}
