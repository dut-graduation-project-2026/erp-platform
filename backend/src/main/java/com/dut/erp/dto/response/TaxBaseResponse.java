package com.dut.erp.dto.response;

import com.dut.erp.enums.TaxComputation;
import java.math.BigDecimal;
import java.util.UUID;

public record TaxBaseResponse(
    UUID id,
    String name,
    TaxComputation computation,
    BigDecimal amount,
    Boolean isArchived
) {}
