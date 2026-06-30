package com.dut.erp.service.impl;

import com.dut.erp.dto.request.CreateInventoryDocumentRequest;
import com.dut.erp.dto.request.InventoryDocumentItemRequest;
import com.dut.erp.dto.response.*;
import com.dut.erp.entity.InventoryBalance;
import com.dut.erp.entity.Warehouse;
import com.dut.erp.enums.DocumentType;
import com.dut.erp.repository.InventoryBalanceRepository;
import com.dut.erp.repository.OrderRepository;
import com.dut.erp.repository.WarehouseRepository;
import com.dut.erp.service.AiService;
import com.dut.erp.service.InventoryDocumentService;
import java.math.BigDecimal;
import com.dut.erp.entity.AnalysisReport;
import com.dut.erp.repository.AnalysisReportRepository;
import com.dut.erp.repository.OrganizationRepository;
import com.dut.erp.util.JsonMapper;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
public class AiServiceImpl implements AiService {

  private final InventoryDocumentService inventoryDocumentService;
  private final RestClient restClient;
  
  private final OrderRepository orderRepository;
  private final WarehouseRepository warehouseRepository;
  private final InventoryBalanceRepository inventoryBalanceRepository;
  private final AnalysisReportRepository analysisReportRepository;
  private final OrganizationRepository organizationRepository;

  public AiServiceImpl(
      InventoryDocumentService inventoryDocumentService,
      @Qualifier("aiServiceRestClient") RestClient restClient,
      OrderRepository orderRepository,
      WarehouseRepository warehouseRepository,
      InventoryBalanceRepository inventoryBalanceRepository,
      AnalysisReportRepository analysisReportRepository,
      OrganizationRepository organizationRepository) {
    this.inventoryDocumentService = inventoryDocumentService;
    this.restClient = restClient;
    this.orderRepository = orderRepository;
    this.warehouseRepository = warehouseRepository;
    this.inventoryBalanceRepository = inventoryBalanceRepository;
    this.analysisReportRepository = analysisReportRepository;
    this.organizationRepository = organizationRepository;
  }

  private Instant getStartOfToday() {
    return LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"))
        .atStartOfDay(ZoneId.of("Asia/Ho_Chi_Minh"))
        .toInstant();
  }

  private <T> Optional<T> getCachedReport(UUID organizationId, String analysisType, Class<T> responseType) {
    List<AnalysisReport> reports = analysisReportRepository.findRecentAnalysis(organizationId, analysisType, getStartOfToday());
    if (!reports.isEmpty()) {
      try {
        T result = JsonMapper.fromJson(reports.get(0).getResultJson(), responseType);
        return Optional.of(result);
      } catch (Exception e) {
        log.error("Failed to deserialize cached analysis report for type: {}", analysisType, e);
      }
    }
    return Optional.empty();
  }

  private <T> Optional<T> getCachedReport(UUID organizationId, String analysisType, ParameterizedTypeReference<T> responseType) {
    List<AnalysisReport> reports = analysisReportRepository.findRecentAnalysis(organizationId, analysisType, getStartOfToday());
    if (!reports.isEmpty()) {
      try {
        T result = JsonMapper.fromJsonList(reports.get(0).getResultJson());
        return Optional.of(result);
      } catch (Exception e) {
        log.error("Failed to deserialize cached analysis report for type: {}", analysisType, e);
      }
    }
    return Optional.empty();
  }

  private void saveReport(UUID organizationId, String analysisType, Object result) {
    try {
      String json = JsonMapper.toJson(result);
      organizationRepository.findById(organizationId).ifPresent(org -> {
        AnalysisReport report = AnalysisReport.builder()
            .organization(org)
            .analysisType(analysisType)
            .resultJson(json)
            .build();
        analysisReportRepository.save(report);
      });
    } catch (Exception e) {
      log.error("Failed to serialize and save analysis report for type: {}", analysisType, e);
    }
  }


  private static final String SALES_FORECAST_PATH = "/analysis/sales-forecast";
  private static final String INVENTORY_ANALYSIS_PATH = "/analysis/inventory";
  private static final String INVENTORY_ALERTS_PATH = "/analysis/inventory-alerts";
  private static final String REORDER_PATH = "/analysis/reorder";
  private static final String DASHBOARD_PATH = "/analysis/dashboard";

  @Override
  public AiSalesForecastResponse getSalesForecast(UUID organizationId, String period) {
    Optional<AiSalesForecastResponse> cached = getCachedReport(organizationId, "SALES_FORECAST", AiSalesForecastResponse.class);
    if (cached.isPresent()) {
      log.info("Returning cached sales forecast for organization: {}", organizationId);
      return cached.get();
    }

    log.info("Fetching sales forecast for organization: {}, period: {}", organizationId, period);
    
    // Fetch all historical daily sales revenue
    Instant startDate = Instant.EPOCH;
    List<Object[]> dailyData = orderRepository.getDailyRevenue(organizationId, startDate);
    
    List<Map<String, Object>> historyList = new ArrayList<>();
    for (Object[] row : dailyData) {
      Map<String, Object> map = new HashMap<>();
      map.put("date", row[0].toString());
      map.put("revenue", row[1]);
      historyList.add(map);
    }

    Map<String, Object> payload = new HashMap<>();
    payload.put("organizationId", organizationId.toString());
    payload.put("history", historyList);

    AiSalesForecastResponse response = postAiData(
        SALES_FORECAST_PATH,
        payload,
        AiSalesForecastResponse.class,
        "Failed to fetch sales forecast from AI service");

    saveReport(organizationId, "SALES_FORECAST", response);
    return response;
  }

  @Override
  public AiInventoryAnalysisResponse getInventoryAnalysis(
      UUID organizationId, boolean forceRefresh) {
    if (!forceRefresh) {
      Optional<AiInventoryAnalysisResponse> cached = getCachedReport(organizationId, "INVENTORY_ANALYSIS", AiInventoryAnalysisResponse.class);
      if (cached.isPresent()) {
        log.info("Returning cached inventory analysis for organization: {}", organizationId);
        return cached.get();
      }
    }

    log.info(
        "Fetching inventory analysis for organization: {}, forceRefresh: {}",
        organizationId,
        forceRefresh);

    Map<String, Object> payload = getInventoryDataPayload(organizationId);
    payload.put("force_refresh", forceRefresh);

    AiInventoryAnalysisResponse response = postAiData(
        INVENTORY_ANALYSIS_PATH,
        payload,
        AiInventoryAnalysisResponse.class,
        "Failed to fetch inventory analysis from AI service");

    saveReport(organizationId, "INVENTORY_ANALYSIS", response);
    return response;
  }

  @Override
  public List<AiProductAbcXyz> getInventoryAlerts(UUID organizationId) {
    Optional<List<AiProductAbcXyz>> cached = getCachedReport(organizationId, "INVENTORY_ALERTS", new ParameterizedTypeReference<List<AiProductAbcXyz>>() {});
    if (cached.isPresent()) {
      log.info("Returning cached inventory alerts for organization: {}", organizationId);
      return cached.get();
    }

    log.info("Fetching inventory alerts for organization: {}", organizationId);
    
    Map<String, Object> payload = getInventoryDataPayload(organizationId);

    List<AiProductAbcXyz> response = postAiData(
        INVENTORY_ALERTS_PATH,
        payload,
        new ParameterizedTypeReference<List<AiProductAbcXyz>>() {},
        "Failed to fetch inventory alerts from AI service");

    saveReport(organizationId, "INVENTORY_ALERTS", response);
    return response;
  }

  @Override
  public AiReorderRecommendationResponse getReorderRecommendations(UUID organizationId) {
    Optional<AiReorderRecommendationResponse> cached = getCachedReport(organizationId, "REORDER_RECOMMENDATIONS", AiReorderRecommendationResponse.class);
    if (cached.isPresent()) {
      log.info("Returning cached reorder recommendations for organization: {}", organizationId);
      return cached.get();
    }

    log.info("Fetching reorder recommendations for organization: {}", organizationId);
    
    Map<String, Object> payload = getInventoryDataPayload(organizationId);

    AiReorderRecommendationResponse response = postAiData(
        REORDER_PATH,
        payload,
        AiReorderRecommendationResponse.class,
        "Failed to fetch reorder recommendations from AI service");

    saveReport(organizationId, "REORDER_RECOMMENDATIONS", response);
    return response;
  }

  @Override
  public AiDashboardSummaryResponse getDashboardSummary(UUID organizationId) {
    Optional<AiDashboardSummaryResponse> cached = getCachedReport(organizationId, "DASHBOARD_SUMMARY", AiDashboardSummaryResponse.class);
    if (cached.isPresent()) {
      log.info("Returning cached dashboard summary for organization: {}", organizationId);
      return cached.get();
    }

    log.info("Fetching dashboard summary for organization: {}", organizationId);
    
    Map<String, Object> payload = new HashMap<>();
    payload.put("organizationId", organizationId.toString());

    // Historical daily sales revenue (All history)
    Instant startDate = Instant.EPOCH;
    List<Object[]> dailyData = orderRepository.getDailyRevenue(organizationId, startDate);
    List<Map<String, Object>> historyList = new ArrayList<>();
    for (Object[] row : dailyData) {
      Map<String, Object> map = new HashMap<>();
      map.put("date", row[0].toString());
      map.put("revenue", row[1]);
      historyList.add(map);
    }
    payload.put("history", historyList);

    // Inventory data payload
    Map<String, Object> invData = getInventoryDataPayload(organizationId);
    payload.put("inventory_data", invData);

    AiDashboardSummaryResponse response = postAiData(
        DASHBOARD_PATH,
        payload,
        AiDashboardSummaryResponse.class,
        "Failed to fetch dashboard summary from AI service");

    saveReport(organizationId, "DASHBOARD_SUMMARY", response);
    return response;
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

  /** Helper to construct inventory and sales history payload */
  private Map<String, Object> getInventoryDataPayload(UUID organizationId) {
    Map<String, Object> payload = new HashMap<>();
    payload.put("organizationId", organizationId.toString());

    // 1. Warehouses
    List<Warehouse> warehouses = warehouseRepository.findAllByOrganizationId(organizationId);
    List<Map<String, Object>> whList = new ArrayList<>();
    for (Warehouse w : warehouses) {
      Map<String, Object> m = new HashMap<>();
      m.put("id", w.getId().toString());
      m.put("name", w.getName());
      whList.add(m);
    }
    payload.put("warehouses", whList);

    // 2. Inventory Balances
    List<InventoryBalance> balances = inventoryBalanceRepository.findAllByOrganizationId(organizationId);
    List<Map<String, Object>> balList = new ArrayList<>();
    for (InventoryBalance ib : balances) {
      Map<String, Object> m = new HashMap<>();
      m.put("warehouseId", ib.getWarehouse().getId().toString());
      m.put("warehouseName", ib.getWarehouse().getName());
      m.put("productId", ib.getProduct().getId().toString());
      m.put("productName", ib.getProduct().getName());
      m.put("quantity", ib.getQuantity());
      m.put("purchasePrice", ib.getProduct().getPurchasePrice());
      m.put("salesPrice", ib.getProduct().getSalesPrice());
      balList.add(m);
    }
    payload.put("balances", balList);

    // 3. Sales History (All history)
    Instant startDate = Instant.EPOCH;
    List<Object[]> salesHistory = orderRepository.getProductSalesHistory(organizationId, startDate);
    List<Map<String, Object>> salesList = new ArrayList<>();
    for (Object[] row : salesHistory) {
      Map<String, Object> m = new HashMap<>();
      m.put("productId", row[0].toString());
      m.put("productName", row[1].toString());
      m.put("date", row[2].toString());
      m.put("quantity", row[3]);
      m.put("price", row[4]);
      salesList.add(m);
    }
    payload.put("sales", salesList);

    return payload;
  }

  /** Generic method to POST data to AI service with automatic error handling */
  private <T> T postAiData(
      String path,
      Object body,
      Class<T> responseType,
      String errorMessage) {
    try {
      return restClient
          .post()
          .uri(uriBuilder -> uriBuilder.path(path).build())
          .body(body)
          .retrieve()
          .body(responseType);
    } catch (Exception e) {
      log.error(errorMessage, e);
      throw new RuntimeException("Failed to connect to AI Service: " + e.getMessage(), e);
    }
  }

  /** Generic method to POST parameterized type data to AI service */
  private <T> T postAiData(
      String path,
      Object body,
      ParameterizedTypeReference<T> responseType,
      String errorMessage) {
    try {
      return restClient
          .post()
          .uri(uriBuilder -> uriBuilder.path(path).build())
          .body(body)
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
        null, // replenishmentRequestId not used for automatic receipt
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
    Object quantityObj = recommendation.get("recommendedQuantity");
    if (quantityObj == null) {
      quantityObj = recommendation.get("quantity");
    }
    if (quantityObj == null) {
      throw new IllegalArgumentException("Missing quantity or recommendedQuantity in recommendation");
    }
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
