package com.dut.erp.dto.response;

import com.dut.erp.enums.TaxComputation;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

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
    UserBaseResponse updatedBy
) {}
