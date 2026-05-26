package com.dut.erp.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record SaleOrderLineResponse(
    UUID id,
    ProductBaseResponse product,
    BigDecimal quantity,
    BigDecimal unitPrice,
    BigDecimal discountPercent,
    BigDecimal subtotal) {}
