package com.dut.erp.dto.response;

import com.dut.erp.enums.TaxComputation;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TaxBaseResponse(
    UUID id, String name, TaxComputation computation, BigDecimal amount, boolean isArchived) {}
