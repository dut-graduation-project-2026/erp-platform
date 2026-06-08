package com.dut.erp.dto.response;

import com.dut.erp.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
    UUID id,
    OrganizationBaseResponse organization,
    PartnerBaseResponse partner,
    LeadBaseResponse lead,
    String orderNumber,
    OrderStatus status,
    Instant deliveryDate,
    Instant expirationDate,
    BigDecimal totalAmount,
    List<OrderItemResponse> items,
    Instant createdAt,
    Instant updatedAt,
    UserBaseResponse createdBy,
    UserBaseResponse updatedBy
) {}
