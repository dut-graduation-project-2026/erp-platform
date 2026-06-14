package com.dut.erp.service.impl;

import com.dut.erp.dto.response.analytics.SalesSummaryResponse;
import com.dut.erp.dto.response.analytics.RevenueTrendPoint;
import com.dut.erp.dto.response.analytics.OrderStatusCount;
import com.dut.erp.dto.response.analytics.CategorySalesDistribution;
import com.dut.erp.dto.response.analytics.LeadStageCount;
import com.dut.erp.dto.response.analytics.PipelineStageSummary;
import com.dut.erp.dto.response.analytics.AssetCategoryDistribution;
import com.dut.erp.dto.response.analytics.StockValuationTrendPoint;
import com.dut.erp.dto.response.analytics.TopProductResponse;
import com.dut.erp.enums.LeadStage;
import com.dut.erp.enums.OrderStatus;
import com.dut.erp.repository.LeadRepository;
import com.dut.erp.repository.OrderRepository;
import com.dut.erp.repository.InventoryBalanceRepository;
import com.dut.erp.repository.InventoryDocumentLineRepository;
import com.dut.erp.repository.StockValuationRepository;
import com.dut.erp.service.AnalyticsService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsServiceImpl implements AnalyticsService {

  private final OrderRepository orderRepository;
  private final StockValuationRepository stockValuationRepository;
  private final LeadRepository leadRepository;
  private final InventoryDocumentLineRepository inventoryDocumentLineRepository;
  private final InventoryBalanceRepository inventoryBalanceRepository;

  @Override
  public SalesSummaryResponse getSalesSummary(UUID organizationId, String periodType, Integer year) {
    log.info("Calculating sales summary analytics for organization {} with periodType {} and year {}", 
        organizationId, periodType, year);

    ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
    int targetYear = (year != null) ? year : now.getYear();

    ZonedDateTime currentStart;
    ZonedDateTime currentEnd;
    ZonedDateTime previousStart;
    ZonedDateTime previousEnd;

    String normalizedPeriod = (periodType != null) ? periodType.toUpperCase() : "YEAR";

    switch (normalizedPeriod) {
      case "WEEK": {
        ZonedDateTime baseDate = (targetYear == now.getYear()) 
            ? now 
            : ZonedDateTime.of(targetYear, 12, 28, 0, 0, 0, 0, ZoneOffset.UTC);
        ZonedDateTime startOfWeek = baseDate.with(DayOfWeek.MONDAY).truncatedTo(ChronoUnit.DAYS);
        currentStart = startOfWeek;
        currentEnd = startOfWeek.plusWeeks(1).minusNanos(1);
        previousStart = startOfWeek.minusWeeks(1);
        previousEnd = startOfWeek.minusNanos(1);
        break;
      }
      case "MONTH": {
        int monthValue = (targetYear == now.getYear()) ? now.getMonthValue() : 12;
        currentStart = ZonedDateTime.of(targetYear, monthValue, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        currentEnd = currentStart.plusMonths(1).minusNanos(1);
        previousStart = currentStart.minusMonths(1);
        previousEnd = currentStart.minusNanos(1);
        break;
      }
      case "QUARTER": {
        int currentQuarter = (targetYear == now.getYear()) ? (now.getMonthValue() - 1) / 3 + 1 : 4;
        int startMonth = (currentQuarter - 1) * 3 + 1;
        currentStart = ZonedDateTime.of(targetYear, startMonth, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        currentEnd = currentStart.plusMonths(3).minusNanos(1);
        previousStart = currentStart.minusMonths(3);
        previousEnd = currentStart.minusNanos(1);
        break;
      }
      default: { // YEAR
        currentStart = ZonedDateTime.of(targetYear, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        currentEnd = currentStart.plusYears(1).minusNanos(1);
        previousStart = currentStart.minusYears(1);
        previousEnd = currentStart.minusNanos(1);
        break;
      }
    }

    Instant currentStartInstant = currentStart.toInstant();
    Instant currentEndInstant = currentEnd.toInstant();
    Instant previousStartInstant = previousStart.toInstant();
    Instant previousEndInstant = previousEnd.toInstant();

    BigDecimal currentRevenue = orderRepository.sumRevenueByOrganizationIdAndDateRange(
        organizationId, currentStartInstant, currentEndInstant);

    BigDecimal previousRevenue = orderRepository.sumRevenueByOrganizationIdAndDateRange(
        organizationId, previousStartInstant, previousEndInstant);

    BigDecimal avgDealSize = orderRepository.avgDealSizeByOrganizationIdAndDateRange(
        organizationId, currentStartInstant, currentEndInstant);

    int activeSalesReps = (int) orderRepository.countActiveSalesRepsByOrganizationIdAndDateRange(
        organizationId, currentStartInstant, currentEndInstant);

    double growthPercent = 0.0;
    if (previousRevenue.compareTo(BigDecimal.ZERO) > 0) {
      growthPercent = currentRevenue.subtract(previousRevenue)
          .divide(previousRevenue, 4, RoundingMode.HALF_UP)
          .multiply(BigDecimal.valueOf(100))
          .doubleValue();
    } else if (currentRevenue.compareTo(BigDecimal.ZERO) > 0) {
      growthPercent = 100.0;
    }

    return new SalesSummaryResponse(
        currentRevenue,
        avgDealSize,
        activeSalesReps,
        previousRevenue,
        growthPercent
    );
  }

  @Override
  public List<RevenueTrendPoint> getRevenueTrend(UUID organizationId, Integer months, Integer year) {
    int targetMonths = (months != null && months > 0) ? months : 6;
    log.info("Calculating gross sales and COGS revenue trend for organization {} for the past {} months (year={})",
        organizationId, targetMonths, year);

    ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
    int targetYear = (year != null) ? year : now.getYear();

    ZonedDateTime endDate;
    if (targetYear == now.getYear()) {
      endDate = now;
    } else {
      endDate = ZonedDateTime.of(targetYear, 12, 1, 0, 0, 0, 0, ZoneOffset.UTC);
    }

    List<RevenueTrendPoint> trendPoints = new ArrayList<>();

    for (int i = targetMonths - 1; i >= 0; i--) {
      ZonedDateTime monthStart = endDate.minusMonths(i).withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);
      ZonedDateTime monthEnd = monthStart.plusMonths(1).minusNanos(1);

      Instant startInstant = monthStart.toInstant();
      Instant endInstant = monthEnd.toInstant();

      BigDecimal grossSales = orderRepository.sumRevenueByOrganizationIdAndDateRange(
          organizationId, startInstant, endInstant);

      BigDecimal cogs = stockValuationRepository.sumCogsByOrganizationIdAndOrderDateRange(
          organizationId, startInstant, endInstant);

      BigDecimal netMargin = grossSales.subtract(cogs);

      trendPoints.add(new RevenueTrendPoint(
          startInstant,
          grossSales,
          cogs,
          netMargin
      ));
    }

    return trendPoints;
  }

  @Override
  public List<OrderStatusCount> getConversionFunnel(UUID organizationId, String periodType, Integer year) {
    log.info("Calculating conversion funnel analytics for organization {} with periodType {} and year {}", 
        organizationId, periodType, year);

    ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
    int targetYear = (year != null) ? year : now.getYear();

    ZonedDateTime currentStart;
    ZonedDateTime currentEnd;

    String normalizedPeriod = (periodType != null) ? periodType.toUpperCase() : "YEAR";

    switch (normalizedPeriod) {
      case "WEEK": {
        ZonedDateTime baseDate = (targetYear == now.getYear()) 
            ? now 
            : ZonedDateTime.of(targetYear, 12, 28, 0, 0, 0, 0, ZoneOffset.UTC);
        ZonedDateTime startOfWeek = baseDate.with(DayOfWeek.MONDAY).truncatedTo(ChronoUnit.DAYS);
        currentStart = startOfWeek;
        currentEnd = startOfWeek.plusWeeks(1).minusNanos(1);
        break;
      }
      case "MONTH": {
        int monthValue = (targetYear == now.getYear()) ? now.getMonthValue() : 12;
        currentStart = ZonedDateTime.of(targetYear, monthValue, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        currentEnd = currentStart.plusMonths(1).minusNanos(1);
        break;
      }
      case "QUARTER": {
        int currentQuarter = (targetYear == now.getYear()) ? (now.getMonthValue() - 1) / 3 + 1 : 4;
        int startMonth = (currentQuarter - 1) * 3 + 1;
        currentStart = ZonedDateTime.of(targetYear, startMonth, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        currentEnd = currentStart.plusMonths(3).minusNanos(1);
        break;
      }
      default: { // YEAR
        currentStart = ZonedDateTime.of(targetYear, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        currentEnd = currentStart.plusYears(1).minusNanos(1);
        break;
      }
    }

    List<OrderStatusCount> dbResults = orderRepository.countOrdersByStatusAndDateRange(
        organizationId, currentStart.toInstant(), currentEnd.toInstant());

    Map<OrderStatus, Long> countsMap = new EnumMap<>(OrderStatus.class);
    for (OrderStatus status : OrderStatus.values()) {
      countsMap.put(status, 0L);
    }

    for (OrderStatusCount result : dbResults) {
      countsMap.put(result.status(), result.count());
    }

    return countsMap.entrySet().stream()
        .map(entry -> new OrderStatusCount(entry.getKey(), entry.getValue()))
        .collect(Collectors.toList());
  }

  @Override
  public List<TopProductResponse> getTopPerformingProducts(
      UUID organizationId, Instant startDate, Instant endDate, int limit) {
    log.info("Fetching top {} performing products for organization {}", limit, organizationId);

    int effectiveLimit = Math.max(1, Math.min(limit, 100));
    Pageable pageable = PageRequest.of(0, effectiveLimit);

    return orderRepository.findTopPerformingProducts(organizationId, startDate, endDate, pageable);
  }

  @Override
  public List<CategorySalesDistribution> getCategorySalesDistribution(
      UUID organizationId, Instant startDate, Instant endDate) {
    log.info("Fetching category sales distribution for organization {}", organizationId);

    List<Object[]> rows =
        orderRepository.findCategorySalesDistribution(organizationId, startDate, endDate);

    if (rows.isEmpty()) {
      return List.of();
    }

    BigDecimal totalRevenue =
        rows.stream()
            .map(row -> (BigDecimal) row[2])
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    List<CategorySalesDistribution> result = new ArrayList<>();
    for (Object[] row : rows) {
      UUID categoryId = (UUID) row[0];
      String categoryName = (String) row[1];
      BigDecimal revenue = (BigDecimal) row[2];
      BigDecimal quantity = (BigDecimal) row[3];
      long orderCount = (long) row[4];

      double percentage = 0.0;
      if (totalRevenue.compareTo(BigDecimal.ZERO) > 0) {
        percentage =
            revenue
                .divide(totalRevenue, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
      }

      result.add(
          new CategorySalesDistribution(categoryId, categoryName, revenue, percentage, quantity, orderCount));
    }

    return result;
  }

  @Override
  public List<LeadStageCount> getLeadStageFunnel(UUID organizationId) {
    log.info("Fetching lead stage funnel for organization {}", organizationId);

    List<Object[]> dbResults = leadRepository.countLeadsByStage(organizationId);

    Map<LeadStage, Long> countsMap = new EnumMap<>(LeadStage.class);
    for (LeadStage stage : LeadStage.values()) {
      countsMap.put(stage, 0L);
    }

    for (Object[] row : dbResults) {
      LeadStage stage = (LeadStage) row[0];
      long count = (long) row[1];
      countsMap.put(stage, count);
    }

    return countsMap.entrySet().stream()
        .map(entry -> new LeadStageCount(entry.getKey(), entry.getValue()))
        .collect(Collectors.toList());
  }

  @Override
  public List<PipelineStageSummary> getPipelineSummary(UUID organizationId) {
    log.info("Fetching pipeline summary for organization {}", organizationId);

    List<Object[]> rows = leadRepository.findPipelineGroupByStage(organizationId);

    Map<LeadStage, PipelineStageSummary> summaryMap = new EnumMap<>(LeadStage.class);
    for (LeadStage stage : LeadStage.values()) {
      summaryMap.put(stage, new PipelineStageSummary(stage, 0, BigDecimal.ZERO, BigDecimal.ZERO, 0.0));
    }

    for (Object[] row : rows) {
      LeadStage stage = (LeadStage) row[0];
      long count = (long) row[1];
      BigDecimal revenue = (BigDecimal) row[2];
      double avgProb = row[3] != null ? ((Number) row[3]).doubleValue() : 0.0;

      BigDecimal weightedRevenue = BigDecimal.ZERO;
      if (count > 0 && revenue.compareTo(BigDecimal.ZERO) > 0) {
        weightedRevenue = revenue
            .multiply(BigDecimal.valueOf(avgProb))
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
      }

      summaryMap.put(stage, new PipelineStageSummary(stage, count, revenue, weightedRevenue, avgProb));
    }

    return List.of(LeadStage.values()).stream()
        .map(summaryMap::get)
        .collect(Collectors.toList());
  }

  @Override
  public List<StockValuationTrendPoint> getStockValuationTrend(
      UUID organizationId, Integer months, Integer year) {
    int targetMonths = (months != null && months > 0) ? months : 12;
    log.info("Calculating stock valuation trend for organization {} for the past {} months (year={})",
        organizationId, targetMonths, year);

    ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
    int targetYear = (year != null) ? year : now.getYear();

    ZonedDateTime endDate;
    if (targetYear == now.getYear()) {
      endDate = now;
    } else {
      endDate = ZonedDateTime.of(targetYear, 12, 1, 0, 0, 0, 0, ZoneOffset.UTC);
    }

    List<StockValuationTrendPoint> points = new ArrayList<>();

    for (int i = targetMonths - 1; i >= 0; i--) {
      ZonedDateTime monthStart = endDate.minusMonths(i).withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);
      ZonedDateTime monthEnd = monthStart.plusMonths(1).minusNanos(1);

      Instant startInstant = monthStart.toInstant();
      Instant endInstant = monthEnd.toInstant();

      BigDecimal inbound = inventoryDocumentLineRepository.sumInboundValuationByOrgAndDateRange(
          organizationId, startInstant, endInstant);

      BigDecimal outbound = inventoryDocumentLineRepository.sumOutboundValuationByOrgAndDateRange(
          organizationId, startInstant, endInstant);

      BigDecimal netChange = inbound.subtract(outbound);

      points.add(new StockValuationTrendPoint(startInstant, inbound, outbound, netChange));
    }

    return points;
  }

  @Override
  public List<AssetCategoryDistribution> getAssetCategoryDistribution(UUID organizationId) {
    log.info("Fetching asset value distribution by category for organization {}", organizationId);

    List<Object[]> rows = inventoryBalanceRepository.findAssetDistributionByCategory(organizationId);

    if (rows.isEmpty()) {
      return List.of();
    }

    BigDecimal totalValue =
        rows.stream()
            .map(row -> (BigDecimal) row[2])
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    List<AssetCategoryDistribution> result = new ArrayList<>();
    for (Object[] row : rows) {
      UUID categoryId = (UUID) row[0];
      String categoryName = (String) row[1];
      BigDecimal value = (BigDecimal) row[2];
      BigDecimal quantity = (BigDecimal) row[3];
      long productCount = (long) row[4];

      double percentage = 0.0;
      if (totalValue.compareTo(BigDecimal.ZERO) > 0) {
        percentage =
            value
                .divide(totalValue, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
      }

      result.add(
          new AssetCategoryDistribution(categoryId, categoryName, value, percentage, quantity, productCount));
    }

    return result;
  }
}
