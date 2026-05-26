package com.dut.erp.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductResponse(
    UUID id,
    String sku,
    String name,
    String barcode,
    BigDecimal price,
    BigDecimal cost,
    BigDecimal weight,
    BigDecimal volume,
    String description,
    BigDecimal minStock,
    Boolean isActive,
    OrganizationBaseResponse organization,
    ProductTemplateBaseResponse productTemplate) {}
