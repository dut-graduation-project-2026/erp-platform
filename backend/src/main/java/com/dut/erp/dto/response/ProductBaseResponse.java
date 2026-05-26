package com.dut.erp.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductBaseResponse(
    UUID id,
    String sku,
    String name,
    String barcode,
    BigDecimal price,
    BigDecimal cost,
    BigDecimal minStock,
    Boolean isActive) {}
