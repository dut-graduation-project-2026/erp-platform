package com.dut.erp.service;

import com.dut.erp.dto.response.analytics.SalesSummaryResponse;
import java.util.UUID;

public interface AnalyticsService {
  SalesSummaryResponse getSalesSummary(UUID organizationId, String periodType, Integer year);
}
