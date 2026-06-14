package com.dut.erp.dto.response.analytics;

import com.dut.erp.enums.OrderStatus;

public record OrderStatusCount(
    OrderStatus status,
    long count
) {}
