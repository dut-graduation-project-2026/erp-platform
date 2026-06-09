package com.dut.erp.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record InventoryBalanceBaseResponse(
    UUID id,
    ProductBaseResponse product,
    BigDecimal quantity
) {}
