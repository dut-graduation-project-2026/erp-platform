package com.dut.erp.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(
    UUID id,
    UUID organizationId,
    UUID orderId,
    ProductBaseResponse product,
    TaxBaseResponse tax,
    BigDecimal quantity,
    BigDecimal unitPrice,
    BigDecimal subtotal
) {}
