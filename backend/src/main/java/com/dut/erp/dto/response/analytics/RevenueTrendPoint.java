package com.dut.erp.dto.response.analytics;

import java.math.BigDecimal;
import java.time.Instant;

public record RevenueTrendPoint(
    Instant date,
    BigDecimal grossSales,
    BigDecimal cogs,
    BigDecimal netMargin
) {}
