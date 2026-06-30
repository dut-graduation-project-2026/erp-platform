package com.dut.erp.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProductResponse(
    UUID id,
    String name,
    String sku,
    String description,
    BigDecimal salesPrice,
    BigDecimal purchasePrice,
    boolean isArchived,
    com.dut.erp.enums.CogsMethod cogsMethod,
    OrganizationBaseResponse organization,
    ProductCategoryBaseResponse category,
    String image,
    Instant createdAt,
    Instant updatedAt,
    UserBaseResponse createdBy,
    UserBaseResponse updatedBy) {}
