package com.dut.erp.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record InventoryBalanceResponse(
    UUID id,
    WarehouseBaseResponse warehouse,
    ProductBaseResponse product,
    BigDecimal quantity,
    Instant updatedAt,
    UserBaseResponse updatedBy
) {}
