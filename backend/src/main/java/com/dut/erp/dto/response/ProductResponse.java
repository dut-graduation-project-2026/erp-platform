package com.dut.erp.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductResponse(
    UUID id,
    String sku,
    String name,
    BigDecimal price,
    String description,
    Boolean isActive,
    OrganizationBaseResponse organization) {}
