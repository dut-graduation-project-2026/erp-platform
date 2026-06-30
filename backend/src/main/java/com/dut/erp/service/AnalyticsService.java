package com.dut.erp.service;

import com.dut.erp.dto.response.analytics.SalesSummaryResponse;
import com.dut.erp.dto.response.analytics.RevenueTrendPoint;
import com.dut.erp.dto.response.analytics.OrderStatusCount;
import com.dut.erp.dto.response.analytics.CategorySalesDistribution;
import com.dut.erp.dto.response.analytics.LeadStageCount;
import com.dut.erp.dto.response.analytics.PipelineStageSummary;
import com.dut.erp.dto.response.analytics.AssetCategoryDistribution;
import com.dut.erp.dto.response.analytics.StockValuationTrendPoint;
import com.dut.erp.dto.response.analytics.TopProductResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface AnalyticsService {
  SalesSummaryResponse getSalesSummary(UUID organizationId, String periodType, Integer year);

  List<RevenueTrendPoint> getRevenueTrend(UUID organizationId, Integer months, Integer year);

  List<OrderStatusCount> getConversionFunnel(UUID organizationId, String periodType, Integer year);

  List<TopProductResponse> getTopPerformingProducts(
      UUID organizationId, Instant startDate, Instant endDate, int limit);

  List<CategorySalesDistribution> getCategorySalesDistribution(
      UUID organizationId, Instant startDate, Instant endDate);

  List<LeadStageCount> getLeadStageFunnel(UUID organizationId);

  List<PipelineStageSummary> getPipelineSummary(UUID organizationId);

  List<StockValuationTrendPoint> getStockValuationTrend(
      UUID organizationId, Integer months, Integer year);

  List<AssetCategoryDistribution> getAssetCategoryDistribution(UUID organizationId);
}
