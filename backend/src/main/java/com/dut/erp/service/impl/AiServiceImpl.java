package com.dut.erp.service.impl;

import com.dut.erp.dto.request.CreateInventoryDocumentRequest;
import com.dut.erp.dto.request.InventoryDocumentItemRequest;
import com.dut.erp.dto.response.*;
import com.dut.erp.enums.DocumentType;
import com.dut.erp.service.AiService;
import com.dut.erp.service.InventoryDocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

  private final InventoryDocumentService inventoryDocumentService;

  @Value("${app.domains.ai-service}")
  private String aiServiceUrl;

  private RestClient getClient() {
    return RestClient.builder().baseUrl(aiServiceUrl).build();
  }

  @Override
  public AiSalesForecastResponse getSalesForecast(UUID organizationId, String period) {
    log.info("Fetching sales forecast for organization {}", organizationId);
    try {
      return getClient().get()
          .uri(uriBuilder -> uriBuilder
              .path("/analysis/sales-forecast")
              .queryParam("organizationId", organizationId.toString())
              .queryParam("period", period)
              .build())
          .retrieve()
          .body(AiSalesForecastResponse.class);
    } catch (Exception e) {
      log.error("Failed to fetch sales forecast from AI service", e);
      throw new RuntimeException("Failed to connect to AI Service: " + e.getMessage(), e);
    }
  }

  @Override
  public AiInventoryAnalysisResponse getInventoryAnalysis(UUID organizationId, boolean forceRefresh) {
    log.info("Fetching inventory analysis for organization {}, forceRefresh={}", organizationId, forceRefresh);
    try {
      return getClient().get()
          .uri(uriBuilder -> uriBuilder
              .path("/analysis/inventory")
              .queryParam("organizationId", organizationId.toString())
              .queryParam("force_refresh", forceRefresh)
              .build())
          .retrieve()
          .body(AiInventoryAnalysisResponse.class);
    } catch (Exception e) {
      log.error("Failed to fetch inventory analysis from AI service", e);
      throw new RuntimeException("Failed to connect to AI Service: " + e.getMessage(), e);
    }
  }

  @Override
  public List<AiProductAbcXyz> getInventoryAlerts(UUID organizationId) {
    log.info("Fetching inventory alerts for organization {}", organizationId);
    try {
      return getClient().get()
          .uri(uriBuilder -> uriBuilder
              .path("/analysis/inventory-alerts")
              .queryParam("organizationId", organizationId.toString())
              .build())
          .retrieve()
          .body(new ParameterizedTypeReference<List<AiProductAbcXyz>>() {});
    } catch (Exception e) {
      log.error("Failed to fetch inventory alerts from AI service", e);
      throw new RuntimeException("Failed to connect to AI Service: " + e.getMessage(), e);
    }
  }

  @Override
  public AiReorderRecommendationResponse getReorderRecommendations(UUID organizationId) {
    log.info("Fetching reorder recommendations for organization {}", organizationId);
    try {
      return getClient().get()
          .uri(uriBuilder -> uriBuilder
              .path("/analysis/reorder")
              .queryParam("organizationId", organizationId.toString())
              .build())
          .retrieve()
          .body(AiReorderRecommendationResponse.class);
    } catch (Exception e) {
      log.error("Failed to fetch reorder recommendations from AI service", e);
      throw new RuntimeException("Failed to connect to AI Service: " + e.getMessage(), e);
    }
  }

  @Override
  @Transactional
  public void confirmReorders(UUID organizationId, UUID warehouseId, List<Map<String, Object>> recommendations) {
    log.info("Confirming AI reorders for organization {}, default warehouse {}", organizationId, warehouseId);
    if (recommendations == null || recommendations.isEmpty()) {
      return;
    }

    // Nhóm các mặt hàng đề xuất theo warehouseId để tạo phiếu nhập tương ứng
    Map<UUID, List<InventoryDocumentItemRequest>> itemsByWarehouse = new HashMap<>();

    for (Map<String, Object> rec : recommendations) {
      try {
        UUID prodId = UUID.fromString(rec.get("productId").toString());
        
        // Parse số lượng
        Object qtyObj = rec.get("quantity");
        BigDecimal qty = (qtyObj instanceof Number) 
            ? BigDecimal.valueOf(((Number) qtyObj).doubleValue())
            : new BigDecimal(qtyObj.toString());

        // Xác định kho đích (nếu không có trong rec thì dùng mặc định)
        UUID targetWhId = warehouseId;
        if (rec.containsKey("warehouseId") && rec.get("warehouseId") != null) {
          targetWhId = UUID.fromString(rec.get("warehouseId").toString());
        }

        InventoryDocumentItemRequest itemReq = new InventoryDocumentItemRequest(prodId, qty);
        itemsByWarehouse.computeIfAbsent(targetWhId, k -> new ArrayList<>()).add(itemReq);
      } catch (Exception e) {
        log.error("Failed to parse AI reorder item: {}", rec, e);
      }
    }

    // Tạo các tài liệu Nhập kho (RECEIPT) tương ứng
    for (Map.Entry<UUID, List<InventoryDocumentItemRequest>> entry : itemsByWarehouse.entrySet()) {
      UUID whId = entry.getKey();
      List<InventoryDocumentItemRequest> items = entry.getValue();

      if (items.isEmpty()) continue;

      CreateInventoryDocumentRequest createRequest = new CreateInventoryDocumentRequest(
          DocumentType.RECEIPT,
          null, // transferSourceWarehouseId
          Instant.now(),
          "Lệnh nhập kho tự động được tạo từ khuyến nghị của AI (Tồn kho dưới điểm ROP)",
          items
      );

      log.info("Creating RECEIPT document for warehouse {} with {} items", whId, items.size());
      inventoryDocumentService.createDocument(organizationId, whId, createRequest);
    }
  }

  @Override
  public AiDashboardSummaryResponse getDashboardSummary(UUID organizationId) {
    log.info("Fetching daily brief dashboard summary for organization {}", organizationId);
    try {
      return getClient().get()
          .uri(uriBuilder -> uriBuilder
              .path("/analysis/dashboard")
              .queryParam("organizationId", organizationId.toString())
              .build())
          .retrieve()
          .body(AiDashboardSummaryResponse.class);
    } catch (Exception e) {
      log.error("Failed to fetch dashboard summary from AI service", e);
      throw new RuntimeException("Failed to connect to AI Service: " + e.getMessage(), e);
    }
  }
}
