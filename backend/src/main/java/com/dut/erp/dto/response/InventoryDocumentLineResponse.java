package com.dut.erp.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record InventoryDocumentLineResponse(
    UUID id,
    UUID productId,
    String productName,
    BigDecimal quantity,
    BigDecimal unitCost,
    BigDecimal valuation
) {}
