package com.dut.erp.dto.response;

import com.dut.erp.enums.OrderStatus;
import java.math.BigDecimal;
import java.util.UUID;

public record OrderBaseResponse(
    UUID id,
    String orderNumber,
    OrderStatus status,
    BigDecimal totalAmount
) {}
