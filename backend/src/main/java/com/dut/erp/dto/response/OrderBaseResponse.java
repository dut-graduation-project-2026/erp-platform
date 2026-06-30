package com.dut.erp.dto.response;

import com.dut.erp.enums.OrderStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record OrderBaseResponse(
    UUID id, 
    String orderNumber,
    PartnerBaseResponse partner, 
    OrderStatus status, 
    BigDecimal totalAmount,
    Instant createdAt,
    UUID warehouseId,
    String warehouseName,
    UUID saleTeamId,
    String saleTeamName) {}
