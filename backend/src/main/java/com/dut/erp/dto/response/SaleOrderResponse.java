package com.dut.erp.dto.response;

import com.dut.erp.enums.SaleOrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SaleOrderResponse(
    UUID id,
    String orderNumber,
    Instant orderDate,
    SaleOrderStatus status,
    BigDecimal totalAmount,
    SalePartnerBaseResponse partner,
    UserBaseResponse salesperson,
    List<SaleOrderLineResponse> lines) {}
