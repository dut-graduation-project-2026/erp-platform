package com.dut.erp.dto.response.analytics;

import java.math.BigDecimal;
import java.util.UUID;

public record CategorySalesDistribution(
    UUID categoryId,
    String categoryName,
    BigDecimal totalRevenue,
    double percentage,
    BigDecimal quantitySold,
    long orderCount
) {}
