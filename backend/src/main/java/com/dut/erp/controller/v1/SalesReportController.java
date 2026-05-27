package com.dut.erp.controller.v1;

import com.dut.erp.dto.response.SalesDashboardResponse;
import com.dut.erp.security.CustomUserDetails;
import com.dut.erp.service.SalesReportService;
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
 * Controller for sales reporting and dashboard analytics within an organization.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/organizations/{organizationId}/reports")
public class SalesReportController {

  private final SalesReportService salesReportService;

  /**
   * Returns a comprehensive sales dashboard including leads, pipeline, orders, revenue,
   * and invoice/payment summaries for the organization.
   */
  @GetMapping("/sales-dashboard")
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and (
          @securityAuthService.hasPermission('sales:select', #organizationId, #userDetails)
          or
          @securityAuthService.hasPermission('crm:select', #organizationId, #userDetails)
          or
          @securityAuthService.hasPermission('finance:select', #organizationId, #userDetails)
        )
      """)
  public ResponseEntity<SalesDashboardResponse> getSalesDashboard(
      @PathVariable UUID organizationId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(salesReportService.getDashboard(organizationId));
  }
}
