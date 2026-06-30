package com.dut.erp.dto.response.analytics;

import java.math.BigDecimal;

public record SalesSummaryResponse(
    BigDecimal ytdNetRevenue,
    BigDecimal avgDealSize,
    int activeSalesReps,
    BigDecimal previousPeriodRevenue,
    double revenueGrowthPercent
) {}
