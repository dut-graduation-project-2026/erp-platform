package com.dut.erp.service.impl;

import com.dut.erp.dto.response.InventoryDashboardResponse;
import com.dut.erp.entity.StockValuation;
import com.dut.erp.exception.ResourceNotFoundException;
import com.dut.erp.repository.OrganizationRepository;
import com.dut.erp.repository.StockValuationRepository;
import com.dut.erp.service.InventoryDashboardService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
public class InventoryDashboardServiceImpl implements InventoryDashboardService {

  private final OrganizationRepository organizationRepository;
  private final StockValuationRepository stockValuationRepository;

  @Override
  public InventoryDashboardResponse getDashboard(UUID organizationId) {
    log.info("Generating inventory dashboard for organization {}", organizationId);

    if (!organizationRepository.existsById(organizationId)) {
      throw new ResourceNotFoundException("Organization not found with id: " + organizationId);
    }

    Instant now = Instant.now();
    Instant thirtyDaysAgo = now.minus(30, ChronoUnit.DAYS);

    // 1. Total Valuation
    BigDecimal totalValuation = stockValuationRepository.sumRemainingValueByOrganizationId(organizationId);

    // 2. Turnover Ratio
    // COGS = sum of negative totalValue of valuation layers for outbound pickings in the last 30 days
    BigDecimal rawCogs = stockValuationRepository.sumOutboundValueByOrganizationIdAndDateAfter(organizationId, thirtyDaysAgo);
    BigDecimal cogs = rawCogs.negate(); // totalValue is negative for outbound, negate to get positive COGS

    // Average Inventory = (Inventory Value 30 days ago + Current Inventory Value) / 2
    BigDecimal beginningValuation = stockValuationRepository.sumTotalValueByOrganizationIdAndDateBefore(organizationId, thirtyDaysAgo);
    BigDecimal endingValuation = stockValuationRepository.sumTotalValueByOrganizationIdAndDateBefore(organizationId, now);
    BigDecimal averageValuation = beginningValuation.add(endingValuation).divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);

    BigDecimal turnoverRatio = BigDecimal.ZERO;
    if (averageValuation.compareTo(BigDecimal.ZERO) > 0) {
      turnoverRatio = cogs.divide(averageValuation, 4, RoundingMode.HALF_UP);
    }

    // 3. Inventory Aging
    List<StockValuation> activeLayers = stockValuationRepository
        .findAllByProductOrganizationIdAndRemainingQtyGreaterThan(organizationId, BigDecimal.ZERO);

    BigDecimal under30 = BigDecimal.ZERO;
    BigDecimal between30And90 = BigDecimal.ZERO;
    BigDecimal over90 = BigDecimal.ZERO;

    for (StockValuation layer : activeLayers) {
      long ageInDays = Duration.between(layer.getCreatedAt(), now).toDays();
      BigDecimal value = layer.getRemainingValue() != null ? layer.getRemainingValue() : BigDecimal.ZERO;

      if (ageInDays < 30) {
        under30 = under30.add(value);
      } else if (ageInDays <= 90) {
        between30And90 = between30And90.add(value);
      } else {
        over90 = over90.add(value);
      }
    }

    Map<String, BigDecimal> agingMap = new HashMap<>();
    agingMap.put("< 30 days", under30);
    agingMap.put("30-90 days", between30And90);
    agingMap.put("> 90 days", over90);

    return new InventoryDashboardResponse(totalValuation, turnoverRatio, agingMap);
  }
}
