package com.dut.erp.service;

import com.dut.erp.dto.response.analytics.SalesSummaryResponse;
import com.dut.erp.dto.response.analytics.RevenueTrendPoint;
import com.dut.erp.dto.response.analytics.OrderStatusCount;
import java.util.List;
import java.util.UUID;

public interface AnalyticsService {
  SalesSummaryResponse getSalesSummary(UUID organizationId, String periodType, Integer year);

  List<RevenueTrendPoint> getRevenueTrend(UUID organizationId, Integer months, Integer year);

  List<OrderStatusCount> getConversionFunnel(UUID organizationId, String periodType, Integer year);
}
