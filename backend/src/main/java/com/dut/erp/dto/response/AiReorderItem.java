package com.dut.erp.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AiReorderItem(
    UUID productId,
    String productName,
    UUID warehouseId,
    String warehouseName,
    BigDecimal currentStock,
    BigDecimal rop,
    BigDecimal eoq,
    BigDecimal recommendedQuantity,
    String urgency,
    String notes
) {}
