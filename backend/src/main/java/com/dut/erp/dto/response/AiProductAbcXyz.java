package com.dut.erp.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AiProductAbcXyz(
    UUID productId,
    String productName,
    String abcClass,
    String xyzClass,
    BigDecimal currentStock,
    BigDecimal rop,
    BigDecimal eoq,
    String status
) {}
