package com.dut.erp.dto.response;

import java.math.BigDecimal;

public record SalesSummaryResponse(
    BigDecimal totalRevenue,
    Long totalOrders,
    BigDecimal averageOrderValue,
    Long completedOrders,
    Long cancelledOrders
) {}
