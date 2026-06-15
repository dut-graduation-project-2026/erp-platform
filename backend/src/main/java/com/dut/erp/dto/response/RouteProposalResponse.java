package com.dut.erp.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record RouteProposalResponse(
    UUID orderId,
    String orderNumber,
    String customerName,
    BigDecimal totalAmount,
    UUID proposedWarehouseId,
    String proposedWarehouseName,
    boolean routable
) {}
