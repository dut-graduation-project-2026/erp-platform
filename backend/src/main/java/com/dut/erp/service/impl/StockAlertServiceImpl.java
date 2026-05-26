package com.dut.erp.service.impl;

import com.dut.erp.dto.response.StockAlertResponse;
import com.dut.erp.entity.Product;
import com.dut.erp.entity.StockQuant;
import com.dut.erp.exception.ResourceNotFoundException;
import com.dut.erp.repository.OrganizationRepository;
import com.dut.erp.repository.ProductRepository;
import com.dut.erp.repository.StockQuantRepository;
import com.dut.erp.service.StockAlertService;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockAlertServiceImpl implements StockAlertService {

  private final OrganizationRepository organizationRepository;
  private final ProductRepository productRepository;
  private final StockQuantRepository stockQuantRepository;

  @Override
  public List<StockAlertResponse> getAlerts(UUID organizationId) {
    log.info("Generating stock alerts for organization {}", organizationId);

    if (!organizationRepository.existsById(organizationId)) {
      throw new ResourceNotFoundException("Organization not found with id: " + organizationId);
    }

    List<StockAlertResponse> alerts = new ArrayList<>();

    // 1. Min Stock Alerts
    List<Product> products = productRepository.findAllByOrganizationId(organizationId);
    List<Object[]> quantSums = stockQuantRepository.sumQuantityByProductForOrganization(organizationId);
    
    Map<UUID, BigDecimal> productStockMap = new HashMap<>();
    for (Object[] row : quantSums) {
      if (row[0] != null) {
        productStockMap.put((UUID) row[0], (BigDecimal) row[1]);
      }
    }

    for (Product product : products) {
      BigDecimal currentStock = productStockMap.getOrDefault(product.getId(), BigDecimal.ZERO);
      BigDecimal minStock = product.getMinStock() != null ? product.getMinStock() : BigDecimal.ZERO;

      if (currentStock.compareTo(minStock) < 0) {
        BigDecimal suggestedPurchaseQty = minStock.subtract(currentStock);
        alerts.add(new StockAlertResponse(
            "MIN_STOCK",
            String.format("Product %s is below minimum stock. Current: %s, Min: %s", product.getSku(), currentStock, minStock),
            product.getId(),
            product.getSku(),
            product.getName(),
            currentStock,
            minStock,
            null,
            null,
            null,
            suggestedPurchaseQty
        ));
      }
    }

    // 2. Expiring Lot Alerts
    Instant thirtyDaysFromNow = Instant.now().plus(30, ChronoUnit.DAYS);
    List<StockQuant> expiringQuants = stockQuantRepository.findExpiringQuantsByOrganization(organizationId, thirtyDaysFromNow);

    for (StockQuant quant : expiringQuants) {
      alerts.add(new StockAlertResponse(
          "EXPIRING_LOT",
          String.format("Lot %s for Product %s is expiring on %s. Quantity in location: %s", 
              quant.getLot().getLotNumber(), quant.getProduct().getSku(), quant.getLot().getExpirationDate(), quant.getQuantity()),
          quant.getProduct().getId(),
          quant.getProduct().getSku(),
          quant.getProduct().getName(),
          quant.getQuantity(),
          null,
          quant.getLot().getId(),
          quant.getLot().getLotNumber(),
          quant.getLot().getExpirationDate(),
          null
      ));
    }

    return alerts;
  }
}
