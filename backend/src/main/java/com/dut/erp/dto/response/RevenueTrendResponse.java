package com.dut.erp.dto.response;

import java.math.BigDecimal;

public record RevenueTrendResponse(
    String date,
    BigDecimal revenue,
    Long orderCount
) {}
