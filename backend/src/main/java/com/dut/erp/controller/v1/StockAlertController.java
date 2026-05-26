package com.dut.erp.controller.v1;

import com.dut.erp.dto.response.StockAlertResponse;
import com.dut.erp.security.CustomUserDetails;
import com.dut.erp.service.StockAlertService;
import java.util.List;
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
 * Controller for managing and querying stock alerts within an organization.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/organizations/{organizationId}/stock/alerts")
public class StockAlertController {

  private final StockAlertService stockAlertService;

  /**
   * Retrieves active stock alerts (minimum stock violations and expiring lots) for the organization.
   */
  @GetMapping
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and (
          @securityAuthService.hasPermission('quants:select', #organizationId, #userDetails)
          or
          @securityAuthService.hasPermission('warehouses:select', #organizationId, #userDetails)
        )
      """)
  public ResponseEntity<List<StockAlertResponse>> getStockAlerts(
      @PathVariable UUID organizationId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(stockAlertService.getAlerts(organizationId));
  }
}
