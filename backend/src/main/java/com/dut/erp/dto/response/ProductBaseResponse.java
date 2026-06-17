package com.dut.erp.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProductBaseResponse(
    UUID id,
    String name,
    String sku,
    BigDecimal price,
    boolean isArchived,
    ProductCategoryBaseResponse category,
    String image
) {}
