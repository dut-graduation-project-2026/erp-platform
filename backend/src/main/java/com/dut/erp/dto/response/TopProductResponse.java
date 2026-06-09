package com.dut.erp.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record TopProductResponse(
    UUID productId,
    String productName,
    BigDecimal totalRevenue,
    BigDecimal quantitySold
) {}
