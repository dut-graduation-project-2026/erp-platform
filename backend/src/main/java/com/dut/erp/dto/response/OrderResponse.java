package com.dut.erp.dto.response;

import com.dut.erp.enums.OrderStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
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
    UserBaseResponse updatedBy,
    UUID warehouseId,
    String warehouseName,
    UUID invoiceId,
    String invoiceNumber,
    com.dut.erp.enums.InvoiceStatus invoiceStatus,
    UUID saleTeamId,
    String saleTeamName) {}

