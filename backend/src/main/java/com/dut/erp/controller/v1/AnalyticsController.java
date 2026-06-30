package com.dut.erp.controller.v1;

import com.dut.erp.dto.response.analytics.SalesSummaryResponse;
import com.dut.erp.dto.response.analytics.RevenueTrendPoint;
import com.dut.erp.dto.response.analytics.OrderStatusCount;
import com.dut.erp.dto.response.analytics.AssetCategoryDistribution;
import com.dut.erp.dto.response.analytics.CategorySalesDistribution;
import com.dut.erp.dto.response.analytics.LeadStageCount;
import com.dut.erp.dto.response.analytics.PipelineStageSummary;
import com.dut.erp.dto.response.analytics.StockValuationTrendPoint;
import com.dut.erp.dto.response.analytics.TopProductResponse;
import com.dut.erp.security.CustomUserDetails;
import com.dut.erp.service.AnalyticsService;
import java.time.Instant;
import java.util.List;
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

  @GetMapping("/sales/revenue-trend")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('orders:read', #organizationId, #userDetails)
      """)
  public ResponseEntity<List<RevenueTrendPoint>> getRevenueTrend(
      @PathVariable UUID organizationId,
      @RequestParam(defaultValue = "6") Integer months,
      @RequestParam(required = false) Integer year,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(analyticsService.getRevenueTrend(organizationId, months, year));
  }

  @GetMapping("/sales/conversion-funnel")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('orders:read', #organizationId, #userDetails)
      """)
  public ResponseEntity<List<OrderStatusCount>> getConversionFunnel(
      @PathVariable UUID organizationId,
      @RequestParam(defaultValue = "YEAR") String periodType,
      @RequestParam(required = false) Integer year,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(analyticsService.getConversionFunnel(organizationId, periodType, year));
  }

  @GetMapping("/sales/top-products")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('orders:read', #organizationId, #userDetails)
      """)
  public ResponseEntity<List<TopProductResponse>> getTopPerformingProducts(
      @PathVariable UUID organizationId,
      @RequestParam(defaultValue = "10") int limit,
      @RequestParam(required = false) Instant startDate,
      @RequestParam(required = false) Instant endDate,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(
        analyticsService.getTopPerformingProducts(organizationId, startDate, endDate, limit));
  }

  @GetMapping("/sales/category-distribution")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('orders:read', #organizationId, #userDetails)
      """)
  public ResponseEntity<List<CategorySalesDistribution>> getCategorySalesDistribution(
      @PathVariable UUID organizationId,
      @RequestParam(required = false) Instant startDate,
      @RequestParam(required = false) Instant endDate,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(
        analyticsService.getCategorySalesDistribution(organizationId, startDate, endDate));
  }

  @GetMapping("/pipeline/lead-funnel")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('leads:read', #organizationId, #userDetails)
      """)
  public ResponseEntity<List<LeadStageCount>> getLeadStageFunnel(
      @PathVariable UUID organizationId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(analyticsService.getLeadStageFunnel(organizationId));
  }

  @GetMapping("/pipeline/summary")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('leads:read', #organizationId, #userDetails)
      """)
  public ResponseEntity<List<PipelineStageSummary>> getPipelineSummary(
      @PathVariable UUID organizationId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(analyticsService.getPipelineSummary(organizationId));
  }

  @GetMapping("/inventory/valuation-trend")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('orders:read', #organizationId, #userDetails)
      """)
  public ResponseEntity<List<StockValuationTrendPoint>> getStockValuationTrend(
      @PathVariable UUID organizationId,
      @RequestParam(defaultValue = "12") Integer months,
      @RequestParam(required = false) Integer year,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(
        analyticsService.getStockValuationTrend(organizationId, months, year));
  }

  @GetMapping("/inventory/asset-distribution")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('orders:read', #organizationId, #userDetails)
      """)
  public ResponseEntity<List<AssetCategoryDistribution>> getAssetCategoryDistribution(
      @PathVariable UUID organizationId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(
        analyticsService.getAssetCategoryDistribution(organizationId));
  }
}
