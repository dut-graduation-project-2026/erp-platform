package com.dut.erp.service.impl;

import com.dut.erp.dto.response.analytics.SalesSummaryResponse;
import com.dut.erp.repository.OrderRepository;
import com.dut.erp.service.AnalyticsService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
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
}
