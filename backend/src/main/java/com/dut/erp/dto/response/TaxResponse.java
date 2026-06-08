package com.dut.erp.dto.response;

import com.dut.erp.enums.TaxComputation;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TaxResponse(
    UUID id,
    String name,
    TaxComputation computation,
    BigDecimal amount,
    String description,
    boolean isArchived,
    OrganizationBaseResponse organization,
    Instant createdAt,
    Instant updatedAt,
    UserBaseResponse createdBy,
    UserBaseResponse updatedBy) {}
