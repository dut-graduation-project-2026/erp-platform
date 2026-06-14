package com.dut.erp.service.impl;

import com.dut.erp.dto.request.CreateInventoryDocumentRequest;
import com.dut.erp.dto.request.InventoryDocumentItemRequest;
import com.dut.erp.dto.response.*;
import com.dut.erp.enums.DocumentType;
import com.dut.erp.service.AiService;
import com.dut.erp.service.InventoryDocumentService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

  private final InventoryDocumentService inventoryDocumentService;

  @Qualifier("aiServiceRestClient")
  private final RestClient restClient;

  private static final String SALES_FORECAST_PATH = "/analysis/sales-forecast";
  private static final String INVENTORY_ANALYSIS_PATH = "/analysis/inventory";
  private static final String INVENTORY_ALERTS_PATH = "/analysis/inventory-alerts";
  private static final String REORDER_PATH = "/analysis/reorder";
  private static final String DASHBOARD_PATH = "/analysis/dashboard";

  @Override
  public AiSalesForecastResponse getSalesForecast(UUID organizationId, String period) {
    log.info("Fetching sales forecast for organization: {}, period: {}", organizationId, period);
    return getAiData(
        SALES_FORECAST_PATH,
        AiSalesForecastResponse.class,
        uriBuilder ->
            uriBuilder
                .queryParam("organizationId", organizationId.toString())
                .queryParam("period", period),
        "Failed to fetch sales forecast from AI service");
  }

  @Override
  public AiInventoryAnalysisResponse getInventoryAnalysis(
      UUID organizationId, boolean forceRefresh) {
    log.info(
        "Fetching inventory analysis for organization: {}, forceRefresh: {}",
        organizationId,
        forceRefresh);
    return getAiData(
        INVENTORY_ANALYSIS_PATH,
        AiInventoryAnalysisResponse.class,
        uriBuilder ->
            uriBuilder
                .queryParam("organizationId", organizationId.toString())
                .queryParam("force_refresh", forceRefresh),
        "Failed to fetch inventory analysis from AI service");
  }

  @Override
  public List<AiProductAbcXyz> getInventoryAlerts(UUID organizationId) {
    log.info("Fetching inventory alerts for organization: {}", organizationId);
    return getAiData(
        INVENTORY_ALERTS_PATH,
        new ParameterizedTypeReference<List<AiProductAbcXyz>>() {},
        uriBuilder -> uriBuilder.queryParam("organizationId", organizationId.toString()),
        "Failed to fetch inventory alerts from AI service");
  }

  @Override
  public AiReorderRecommendationResponse getReorderRecommendations(UUID organizationId) {
    log.info("Fetching reorder recommendations for organization: {}", organizationId);
    return getAiData(
        REORDER_PATH,
        AiReorderRecommendationResponse.class,
        uriBuilder -> uriBuilder.queryParam("organizationId", organizationId.toString()),
        "Failed to fetch reorder recommendations from AI service");
  }

  @Override
  public AiDashboardSummaryResponse getDashboardSummary(UUID organizationId) {
    log.info("Fetching dashboard summary for organization: {}", organizationId);
    return getAiData(
        DASHBOARD_PATH,
        AiDashboardSummaryResponse.class,
        uriBuilder -> uriBuilder.queryParam("organizationId", organizationId.toString()),
        "Failed to fetch dashboard summary from AI service");
  }

  @Override
  @Transactional
  public void confirmReorders(
      UUID organizationId, UUID warehouseId, List<Map<String, Object>> recommendations) {
    log.info(
        "Confirming AI reorders for organization: {}, warehouse: {}, count: {}",
        organizationId,
        warehouseId,
        recommendations != null ? recommendations.size() : 0);

    if (recommendations == null || recommendations.isEmpty()) {
      log.debug("No recommendations to confirm");
      return;
    }

    Map<UUID, List<InventoryDocumentItemRequest>> itemsByWarehouse =
        groupRecommendationsByWarehouse(recommendations, warehouseId);

    createReceiptDocuments(organizationId, itemsByWarehouse);
  }

  // ==================== Private helper methods ====================

  /** Generic method to fetch data from AI service with automatic error handling */
  private <T> T getAiData(
      String path,
      Class<T> responseType,
      java.util.function.Function<UriBuilder, UriBuilder> uriCustomizer,
      String errorMessage) {
    try {
      return restClient
          .get()
          .uri(
              uriBuilder -> {
                UriBuilder builder = uriBuilder.path(path);
                return uriCustomizer.apply(builder).build();
              })
          .retrieve()
          .body(responseType);
    } catch (Exception e) {
      log.error(errorMessage, e);
      throw new RuntimeException("Failed to connect to AI Service: " + e.getMessage(), e);
    }
  }

  /** Generic method to fetch parameterized type data from AI service */
  private <T> T getAiData(
      String path,
      ParameterizedTypeReference<T> responseType,
      java.util.function.Function<UriBuilder, UriBuilder> uriCustomizer,
      String errorMessage) {
    try {
      return restClient
          .get()
          .uri(
              uriBuilder -> {
                UriBuilder builder = uriBuilder.path(path);
                return uriCustomizer.apply(builder).build();
              })
          .retrieve()
          .body(responseType);
    } catch (Exception e) {
      log.error(errorMessage, e);
      throw new RuntimeException("Failed to connect to AI Service: " + e.getMessage(), e);
    }
  }

  /** Group recommendations by warehouse ID for batch processing */
  private Map<UUID, List<InventoryDocumentItemRequest>> groupRecommendationsByWarehouse(
      List<Map<String, Object>> recommendations, UUID defaultWarehouseId) {
    Map<UUID, List<InventoryDocumentItemRequest>> itemsByWarehouse = new HashMap<>();

    for (Map<String, Object> recommendation : recommendations) {
      try {
        UUID productId = parseProductId(recommendation);
        BigDecimal quantity = parseQuantity(recommendation);
        UUID targetWarehouseId = parseWarehouseId(recommendation, defaultWarehouseId);

        InventoryDocumentItemRequest item = new InventoryDocumentItemRequest(productId, quantity);
        itemsByWarehouse.computeIfAbsent(targetWarehouseId, k -> new ArrayList<>()).add(item);

      } catch (Exception e) {
        log.warn("Failed to parse AI reorder recommendation: {}", recommendation, e);
      }
    }

    return itemsByWarehouse;
  }

  /** Create RECEIPT inventory documents for confirmed reorders */
  private void createReceiptDocuments(
      UUID organizationId, Map<UUID, List<InventoryDocumentItemRequest>> itemsByWarehouse) {
    for (Map.Entry<UUID, List<InventoryDocumentItemRequest>> entry : itemsByWarehouse.entrySet()) {
      UUID warehouseId = entry.getKey();
      List<InventoryDocumentItemRequest> items = entry.getValue();

      if (items.isEmpty()) {
        continue;
      }

      CreateInventoryDocumentRequest request = buildReceiptRequest(items);
      log.debug(
          "Creating RECEIPT document for warehouse: {}, items count: {}",
          warehouseId,
          items.size());

      inventoryDocumentService.createDocument(organizationId, warehouseId, request);
    }
  }

  /** Build a RECEIPT document request for inventory reorder */
  private CreateInventoryDocumentRequest buildReceiptRequest(
      List<InventoryDocumentItemRequest> items) {
    return new CreateInventoryDocumentRequest(
        DocumentType.RECEIPT,
        null, // transferSourceWarehouseId not used for RECEIPT
        Instant.now(),
        "Automatic receipt created from AI reorder recommendations (stock below ROP)",
        items);
  }

  /** Parse product ID from recommendation map */
  private UUID parseProductId(Map<String, Object> recommendation) {
    return UUID.fromString(recommendation.get("productId").toString());
  }

  /** Parse quantity from recommendation map with type conversion */
  private BigDecimal parseQuantity(Map<String, Object> recommendation) {
    Object quantityObj = recommendation.get("quantity");
    if (quantityObj instanceof Number) {
      return BigDecimal.valueOf(((Number) quantityObj).doubleValue());
    }
    return new BigDecimal(quantityObj.toString());
  }

  /** Parse warehouse ID from recommendation map with fallback to default */
  private UUID parseWarehouseId(Map<String, Object> recommendation, UUID defaultWarehouseId) {
    if (recommendation.containsKey("warehouseId") && recommendation.get("warehouseId") != null) {
      return UUID.fromString(recommendation.get("warehouseId").toString());
    }
    return defaultWarehouseId;
  }
}
