package com.dut.erp.service;

import com.dut.erp.dto.response.CrmBySalespersonResponse;
import com.dut.erp.dto.response.CrmStageDistributionResponse;
import com.dut.erp.dto.response.CrmSummaryResponse;
import com.dut.erp.dto.response.LeadTrendResponse;
import com.dut.erp.dto.response.RevenueTrendResponse;
import com.dut.erp.dto.response.SalesBySalespersonResponse;
import com.dut.erp.dto.response.SalesSummaryResponse;
import com.dut.erp.dto.response.TopCustomerResponse;
import com.dut.erp.dto.response.TopProductResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface AnalyticsService {
  SalesSummaryResponse getSalesSummary(UUID organizationId, Instant startDate, Instant endDate);

  List<RevenueTrendResponse> getSalesRevenueTrend(
      UUID organizationId, Instant startDate, Instant endDate, String period);

  List<TopCustomerResponse> getTopCustomers(
      UUID organizationId, Instant startDate, Instant endDate, Integer limit);

  List<TopProductResponse> getTopProducts(
      UUID organizationId, Instant startDate, Instant endDate, Integer limit);

  List<SalesBySalespersonResponse> getSalesBySalesperson(
      UUID organizationId, Instant startDate, Instant endDate);

  CrmSummaryResponse getCrmSummary(UUID organizationId, Instant startDate, Instant endDate);

  List<CrmStageDistributionResponse> getCrmStageDistribution(
      UUID organizationId, Instant startDate, Instant endDate);

  List<LeadTrendResponse> getCrmLeadTrend(
      UUID organizationId, Instant startDate, Instant endDate, String period);

  List<CrmBySalespersonResponse> getCrmBySalesperson(
      UUID organizationId, Instant startDate, Instant endDate);
}
