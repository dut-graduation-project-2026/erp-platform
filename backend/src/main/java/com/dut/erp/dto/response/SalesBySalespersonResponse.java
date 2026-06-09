package com.dut.erp.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record SalesBySalespersonResponse(
    UUID salespersonId,
    String salespersonName,
    BigDecimal totalRevenue,
    Long orderCount
) {}
