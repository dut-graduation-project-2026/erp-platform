package com.dut.erp.dto.response;

import com.dut.erp.enums.SaleOrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SaleOrderBaseResponse(
    UUID id,
    String orderNumber,
    Instant orderDate,
    SaleOrderStatus status,
    BigDecimal totalAmount,
    SalePartnerBaseResponse partner) {}
