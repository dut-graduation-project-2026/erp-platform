package com.dut.erp.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record OrderItemResponse(
    UUID id,
    UUID organizationId,
    UUID orderId,
    ProductBaseResponse product,
    TaxBaseResponse tax,
    BigDecimal quantity,
    BigDecimal unitPrice,
    BigDecimal subtotal) {}
