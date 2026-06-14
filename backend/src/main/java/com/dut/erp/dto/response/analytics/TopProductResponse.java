package com.dut.erp.dto.response.analytics;

import java.math.BigDecimal;
import java.util.UUID;

public record TopProductResponse(
    UUID productId,
    String productName,
    String categoryName,
    BigDecimal totalRevenue,
    BigDecimal quantitySold,
    long orderCount
) {}
