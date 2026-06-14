package com.dut.erp.controller.v1;

import com.dut.erp.dto.response.analytics.SalesSummaryResponse;
import com.dut.erp.security.CustomUserDetails;
import com.dut.erp.service.AnalyticsService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/organizations/{organizationId}/analytics")
public class AnalyticsController {

  private final AnalyticsService analyticsService;

  @GetMapping("/sales/summary")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('orders:read', #organizationId, #userDetails)
      """)
  public ResponseEntity<SalesSummaryResponse> getSalesSummary(
      @PathVariable UUID organizationId,
      @RequestParam(defaultValue = "YEAR") String periodType,
      @RequestParam(required = false) Integer year,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(analyticsService.getSalesSummary(organizationId, periodType, year));
  }
}
