package com.dut.erp.dto.response;

import java.math.BigDecimal;
import java.util.Map;

public record InventoryDashboardResponse(
    BigDecimal totalValuation,
    BigDecimal turnoverRatio,
    Map<String, BigDecimal> inventoryAging
) {}
