package com.dut.erp.service;

import com.dut.erp.dto.response.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface AiService {

  AiSalesForecastResponse getSalesForecast(UUID organizationId, String period);

  AiInventoryAnalysisResponse getInventoryAnalysis(UUID organizationId, boolean forceRefresh);

  List<AiProductAbcXyz> getInventoryAlerts(UUID organizationId);

  AiReorderRecommendationResponse getReorderRecommendations(UUID organizationId);

  void confirmReorders(
      UUID organizationId, UUID warehouseId, List<Map<String, Object>> recommendations);

  AiDashboardSummaryResponse getDashboardSummary(UUID organizationId);
}
