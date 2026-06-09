package com.dut.erp.controller.v1;

import com.dut.erp.dto.response.CrmBySalespersonResponse;
import com.dut.erp.dto.response.CrmStageDistributionResponse;
import com.dut.erp.dto.response.CrmSummaryResponse;
import com.dut.erp.dto.response.LeadTrendResponse;
import com.dut.erp.dto.response.RevenueTrendResponse;
import com.dut.erp.dto.response.SalesBySalespersonResponse;
import com.dut.erp.dto.response.SalesSummaryResponse;
import com.dut.erp.dto.response.TopCustomerResponse;
import com.dut.erp.dto.response.TopProductResponse;
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
      @RequestParam(required = false) Instant startDate,
      @RequestParam(required = false) Instant endDate,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(analyticsService.getSalesSummary(organizationId, startDate, endDate));
  }

  @GetMapping("/sales/revenue-trend")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('orders:read', #organizationId, #userDetails)
      """)
  public ResponseEntity<List<RevenueTrendResponse>> getSalesRevenueTrend(
      @PathVariable UUID organizationId,
      @RequestParam(required = false) Instant startDate,
      @RequestParam(required = false) Instant endDate,
      @RequestParam(required = false) String period,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(analyticsService.getSalesRevenueTrend(organizationId, startDate, endDate, period));
  }

  @GetMapping("/sales/top-customers")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('orders:read', #organizationId, #userDetails)
      """)
  public ResponseEntity<List<TopCustomerResponse>> getTopCustomers(
      @PathVariable UUID organizationId,
      @RequestParam(required = false) Instant startDate,
      @RequestParam(required = false) Instant endDate,
      @RequestParam(required = false) Integer limit,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(analyticsService.getTopCustomers(organizationId, startDate, endDate, limit));
  }

  @GetMapping("/sales/top-products")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('orders:read', #organizationId, #userDetails)
      """)
  public ResponseEntity<List<TopProductResponse>> getTopProducts(
      @PathVariable UUID organizationId,
      @RequestParam(required = false) Instant startDate,
      @RequestParam(required = false) Instant endDate,
      @RequestParam(required = false) Integer limit,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(analyticsService.getTopProducts(organizationId, startDate, endDate, limit));
  }

  @GetMapping("/sales/by-salesperson")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('orders:read', #organizationId, #userDetails)
      """)
  public ResponseEntity<List<SalesBySalespersonResponse>> getSalesBySalesperson(
      @PathVariable UUID organizationId,
      @RequestParam(required = false) Instant startDate,
      @RequestParam(required = false) Instant endDate,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(analyticsService.getSalesBySalesperson(organizationId, startDate, endDate));
  }

  @GetMapping("/crm/summary")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('leads:read', #organizationId, #userDetails)
      """)
  public ResponseEntity<CrmSummaryResponse> getCrmSummary(
      @PathVariable UUID organizationId,
      @RequestParam(required = false) Instant startDate,
      @RequestParam(required = false) Instant endDate,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(analyticsService.getCrmSummary(organizationId, startDate, endDate));
  }

  @GetMapping("/crm/stage-distribution")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('leads:read', #organizationId, #userDetails)
      """)
  public ResponseEntity<List<CrmStageDistributionResponse>> getCrmStageDistribution(
      @PathVariable UUID organizationId,
      @RequestParam(required = false) Instant startDate,
      @RequestParam(required = false) Instant endDate,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(analyticsService.getCrmStageDistribution(organizationId, startDate, endDate));
  }

  @GetMapping("/crm/lead-trend")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('leads:read', #organizationId, #userDetails)
      """)
  public ResponseEntity<List<LeadTrendResponse>> getCrmLeadTrend(
      @PathVariable UUID organizationId,
      @RequestParam(required = false) Instant startDate,
      @RequestParam(required = false) Instant endDate,
      @RequestParam(required = false) String period,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(analyticsService.getCrmLeadTrend(organizationId, startDate, endDate, period));
  }

  @GetMapping("/crm/by-salesperson")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('leads:read', #organizationId, #userDetails)
      """)
  public ResponseEntity<List<CrmBySalespersonResponse>> getCrmBySalesperson(
      @PathVariable UUID organizationId,
      @RequestParam(required = false) Instant startDate,
      @RequestParam(required = false) Instant endDate,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(analyticsService.getCrmBySalesperson(organizationId, startDate, endDate));
  }
}
