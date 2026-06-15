package com.dut.erp.controller.v1;

import com.dut.erp.dto.response.*;
import com.dut.erp.security.CustomUserDetails;
import com.dut.erp.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/organizations/{organizationId}/ai")
public class AiController {

  private final AiService aiService;

  @GetMapping("/sales/forecast")
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('orders:read', #organizationId, #userDetails)
      """)
  public ResponseEntity<AiSalesForecastResponse> getSalesForecast(
      @PathVariable UUID organizationId,
      @RequestParam(defaultValue = "30d") String period,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(aiService.getSalesForecast(organizationId, period));
  }

  @GetMapping("/inventory/analysis")
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('warehouses:read', #organizationId, #userDetails)
      """)
  public ResponseEntity<AiInventoryAnalysisResponse> getInventoryAnalysis(
      @PathVariable UUID organizationId,
      @RequestParam(defaultValue = "false") boolean forceRefresh,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(aiService.getInventoryAnalysis(organizationId, forceRefresh));
  }

  @GetMapping("/inventory/alerts")
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('warehouses:read', #organizationId, #userDetails)
      """)
  public ResponseEntity<List<AiProductAbcXyz>> getInventoryAlerts(
      @PathVariable UUID organizationId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(aiService.getInventoryAlerts(organizationId));
  }

  @GetMapping("/reorder/recommendations")
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('warehouses:read', #organizationId, #userDetails)
      """)
  public ResponseEntity<AiReorderRecommendationResponse> getReorderRecommendations(
      @PathVariable UUID organizationId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(aiService.getReorderRecommendations(organizationId));
  }

  @PostMapping("/reorder/confirm")
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('warehouses:write', #organizationId, #userDetails)
      """)
  public ResponseEntity<Void> confirmReorders(
      @PathVariable UUID organizationId,
      @RequestParam UUID warehouseId,
      @RequestBody List<Map<String, Object>> recommendations,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    aiService.confirmReorders(organizationId, warehouseId, recommendations);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/dashboard/summary")
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
      """)
  public ResponseEntity<AiDashboardSummaryResponse> getDashboardSummary(
      @PathVariable UUID organizationId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(aiService.getDashboardSummary(organizationId));
  }
}
