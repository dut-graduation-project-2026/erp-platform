package com.dut.erp.dto.response;

import com.dut.erp.enums.CogsMethod;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record StockValuationResponse(
    UUID id,
    UUID inventoryDocumentLineId,
    UUID productId,
    String productName,
    BigDecimal quantity,
    BigDecimal unitCost,
    BigDecimal totalValuation,
    BigDecimal salesPrice,
    CogsMethod method,
    Instant createdAt
) {}
