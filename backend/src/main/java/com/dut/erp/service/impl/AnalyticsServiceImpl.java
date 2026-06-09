package com.dut.erp.service.impl;

import com.dut.erp.dto.response.CrmBySalespersonResponse;
import com.dut.erp.dto.response.CrmStageDistributionResponse;
import com.dut.erp.dto.response.CrmSummaryResponse;
import com.dut.erp.dto.response.LeadTrendResponse;
import com.dut.erp.dto.response.RevenueTrendResponse;
import com.dut.erp.dto.response.SalesBySalespersonResponse;
import com.dut.erp.dto.response.SalesSummaryResponse;
import com.dut.erp.dto.response.TopCustomerResponse;
import com.dut.erp.dto.response.TopProductResponse;
import com.dut.erp.repository.LeadRepository;
import com.dut.erp.repository.OrderRepository;
import com.dut.erp.repository.projection.CrmSummaryProjection;
import com.dut.erp.repository.projection.SalesSummaryProjection;
import com.dut.erp.service.AnalyticsService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsServiceImpl implements AnalyticsService {

  private final OrderRepository orderRepository;
  private final LeadRepository leadRepository;

  @Override
  public SalesSummaryResponse getSalesSummary(
      UUID organizationId, Instant startDate, Instant endDate) {
    log.info(
        "Calculating sales summary for organization {} from {} to {}",
        organizationId,
        startDate,
        endDate);
    Instant start = getOrDefaultStartDate(startDate);
    Instant end = getOrDefaultEndDate(endDate);

    SalesSummaryProjection projection = orderRepository.getSalesSummary(organizationId, start, end);
    if (projection == null) {
      return new SalesSummaryResponse(BigDecimal.ZERO, 0L, BigDecimal.ZERO, 0L, 0L);
    }

    return new SalesSummaryResponse(
        projection.getTotalRevenue() != null ? projection.getTotalRevenue() : BigDecimal.ZERO,
        projection.getTotalOrders() != null ? projection.getTotalOrders() : 0L,
        projection.getAverageOrderValue() != null
            ? projection.getAverageOrderValue()
            : BigDecimal.ZERO,
        projection.getCompletedOrders() != null ? projection.getCompletedOrders() : 0L,
        projection.getCancelledOrders() != null ? projection.getCancelledOrders() : 0L);
  }

  @Override
  public List<RevenueTrendResponse> getSalesRevenueTrend(
      UUID organizationId, Instant startDate, Instant endDate, String period) {
    log.info(
        "Calculating sales revenue trend for organization {} from {} to {}, period: {}",
        organizationId,
        startDate,
        endDate,
        period);
    Instant start = getOrDefaultStartDate(startDate);
    Instant end = getOrDefaultEndDate(endDate);
    String grpPeriod = getOrDefaultPeriod(period);

    if ("week".equalsIgnoreCase(grpPeriod)) {
      return orderRepository.getWeeklyRevenueTrend(organizationId, start, end).stream()
          .map(p -> new RevenueTrendResponse(p.getDateVal(), p.getRevenue(), p.getOrderCount()))
          .collect(Collectors.toList());
    } else if ("month".equalsIgnoreCase(grpPeriod)) {
      return orderRepository.getMonthlyRevenueTrend(organizationId, start, end).stream()
          .map(p -> new RevenueTrendResponse(p.getDateVal(), p.getRevenue(), p.getOrderCount()))
          .collect(Collectors.toList());
    } else {
      return orderRepository.getDailyRevenueTrend(organizationId, start, end).stream()
          .map(p -> new RevenueTrendResponse(p.getDateVal(), p.getRevenue(), p.getOrderCount()))
          .collect(Collectors.toList());
    }
  }

  @Override
  public List<TopCustomerResponse> getTopCustomers(
      UUID organizationId, Instant startDate, Instant endDate, Integer limit) {
    log.info(
        "Calculating top customers for organization {} from {} to {}, limit: {}",
        organizationId,
        startDate,
        endDate,
        limit);
    Instant start = getOrDefaultStartDate(startDate);
    Instant end = getOrDefaultEndDate(endDate);
    int limitVal = getOrDefaultLimit(limit);

    return orderRepository.getTopCustomers(organizationId, start, end, limitVal).stream()
        .map(
            p ->
                new TopCustomerResponse(
                    p.getPartnerId(), p.getPartnerName(), p.getTotalSpend(), p.getOrderCount()))
        .collect(Collectors.toList());
  }

  @Override
  public List<TopProductResponse> getTopProducts(
      UUID organizationId, Instant startDate, Instant endDate, Integer limit) {
    log.info(
        "Calculating top products for organization {} from {} to {}, limit: {}",
        organizationId,
        startDate,
        endDate,
        limit);
    Instant start = getOrDefaultStartDate(startDate);
    Instant end = getOrDefaultEndDate(endDate);
    int limitVal = getOrDefaultLimit(limit);

    return orderRepository.getTopProducts(organizationId, start, end, limitVal).stream()
        .map(
            p ->
                new TopProductResponse(
                    p.getProductId(), p.getProductName(), p.getTotalRevenue(), p.getQuantitySold()))
        .collect(Collectors.toList());
  }

  @Override
  public List<SalesBySalespersonResponse> getSalesBySalesperson(
      UUID organizationId, Instant startDate, Instant endDate) {
    log.info(
        "Calculating sales by salesperson for organization {} from {} to {}",
        organizationId,
        startDate,
        endDate);
    Instant start = getOrDefaultStartDate(startDate);
    Instant end = getOrDefaultEndDate(endDate);

    return orderRepository.getSalesBySalesperson(organizationId, start, end).stream()
        .map(
            p ->
                new SalesBySalespersonResponse(
                    p.getSalespersonId(),
                    p.getSalespersonName(),
                    p.getTotalRevenue(),
                    p.getOrderCount()))
        .collect(Collectors.toList());
  }

  @Override
  public CrmSummaryResponse getCrmSummary(UUID organizationId, Instant startDate, Instant endDate) {
    log.info(
        "Calculating CRM summary for organization {} from {} to {}",
        organizationId,
        startDate,
        endDate);
    Instant start = getOrDefaultStartDate(startDate);
    Instant end = getOrDefaultEndDate(endDate);

    CrmSummaryProjection projection = leadRepository.getCrmSummary(organizationId, start, end);
    if (projection == null) {
      return new CrmSummaryResponse(0L, 0L, 0L, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    Long totalLeads = projection.getTotalLeads() != null ? projection.getTotalLeads() : 0L;
    Long wonLeads = projection.getWonLeads() != null ? projection.getWonLeads() : 0L;
    Long lostLeads = projection.getLostLeads() != null ? projection.getLostLeads() : 0L;
    BigDecimal expectedRevenue =
        projection.getExpectedRevenue() != null ? projection.getExpectedRevenue() : BigDecimal.ZERO;
    BigDecimal wonRevenue =
        projection.getWonRevenue() != null ? projection.getWonRevenue() : BigDecimal.ZERO;

    BigDecimal conversionRate = BigDecimal.ZERO;
    if (totalLeads > 0) {
      conversionRate =
          BigDecimal.valueOf(wonLeads)
              .multiply(BigDecimal.valueOf(100))
              .divide(BigDecimal.valueOf(totalLeads), 2, RoundingMode.HALF_UP);
    }

    return new CrmSummaryResponse(
        totalLeads, wonLeads, lostLeads, conversionRate, expectedRevenue, wonRevenue);
  }

  @Override
  public List<CrmStageDistributionResponse> getCrmStageDistribution(
      UUID organizationId, Instant startDate, Instant endDate) {
    log.info(
        "Calculating CRM stage distribution for organization {} from {} to {}",
        organizationId,
        startDate,
        endDate);
    Instant start = getOrDefaultStartDate(startDate);
    Instant end = getOrDefaultEndDate(endDate);

    return leadRepository.getCrmStageDistribution(organizationId, start, end).stream()
        .map(
            p ->
                new CrmStageDistributionResponse(
                    p.getStage(), p.getLeadCount(), p.getExpectedRevenue()))
        .collect(Collectors.toList());
  }

  @Override
  public List<LeadTrendResponse> getCrmLeadTrend(
      UUID organizationId, Instant startDate, Instant endDate, String period) {
    log.info(
        "Calculating CRM lead trend for organization {} from {} to {}, period: {}",
        organizationId,
        startDate,
        endDate,
        period);
    Instant start = getOrDefaultStartDate(startDate);
    Instant end = getOrDefaultEndDate(endDate);
    String grpPeriod = getOrDefaultPeriod(period);

    if ("week".equalsIgnoreCase(grpPeriod)) {
      return leadRepository.getWeeklyLeadTrend(organizationId, start, end).stream()
          .map(p -> new LeadTrendResponse(p.getDateVal(), p.getLeadCount()))
          .collect(Collectors.toList());
    } else if ("month".equalsIgnoreCase(grpPeriod)) {
      return leadRepository.getMonthlyLeadTrend(organizationId, start, end).stream()
          .map(p -> new LeadTrendResponse(p.getDateVal(), p.getLeadCount()))
          .collect(Collectors.toList());
    } else {
      return leadRepository.getDailyLeadTrend(organizationId, start, end).stream()
          .map(p -> new LeadTrendResponse(p.getDateVal(), p.getLeadCount()))
          .collect(Collectors.toList());
    }
  }

  @Override
  public List<CrmBySalespersonResponse> getCrmBySalesperson(
      UUID organizationId, Instant startDate, Instant endDate) {
    log.info(
        "Calculating CRM salesperson performance for organization {} from {} to {}",
        organizationId,
        startDate,
        endDate);
    Instant start = getOrDefaultStartDate(startDate);
    Instant end = getOrDefaultEndDate(endDate);

    return leadRepository.getCrmBySalesperson(organizationId, start, end).stream()
        .map(
            p ->
                new CrmBySalespersonResponse(
                    p.getSalespersonId(),
                    p.getSalespersonName(),
                    p.getTotalLeads() != null ? p.getTotalLeads() : 0L,
                    p.getWonLeads() != null ? p.getWonLeads() : 0L,
                    p.getExpectedRevenue() != null ? p.getExpectedRevenue() : BigDecimal.ZERO,
                    p.getWonRevenue() != null ? p.getWonRevenue() : BigDecimal.ZERO))
        .collect(Collectors.toList());
  }

  private Instant getOrDefaultStartDate(Instant startDate) {
    return startDate != null ? startDate : Instant.now().minus(30, ChronoUnit.DAYS);
  }

  private Instant getOrDefaultEndDate(Instant endDate) {
    return endDate != null ? endDate : Instant.now();
  }

  private String getOrDefaultPeriod(String period) {
    if (period == null
        || (!period.equalsIgnoreCase("day")
            && !period.equalsIgnoreCase("week")
            && !period.equalsIgnoreCase("month"))) {
      return "day";
    }
    return period.toLowerCase();
  }

  private int getOrDefaultLimit(Integer limit) {
    return (limit != null && limit > 0) ? limit : 10;
  }
}
