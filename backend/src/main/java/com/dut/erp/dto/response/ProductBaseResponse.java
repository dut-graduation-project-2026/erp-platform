package com.dut.erp.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProductBaseResponse(
    UUID id,
    String name,
    String sku,
    BigDecimal salesPrice,
    BigDecimal purchasePrice,
    boolean isArchived,
    ProductCategoryBaseResponse category,
    com.dut.erp.enums.CogsMethod cogsMethod,
    String image
) {}
