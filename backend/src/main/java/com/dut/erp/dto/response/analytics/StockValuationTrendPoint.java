package com.dut.erp.dto.response.analytics;

import java.math.BigDecimal;
import java.time.Instant;

public record StockValuationTrendPoint(
    Instant month,
    BigDecimal inboundValue,
    BigDecimal outboundValue,
    BigDecimal netChange
) {}
