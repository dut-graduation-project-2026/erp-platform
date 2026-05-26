package com.dut.erp.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

public record UpdateProductRequest(
    @NotBlank(message = "Product name cannot be blank")
        @Size(max = 255, message = "Product name must not exceed 255 characters")
        String name,
    @NotNull(message = "Price is required")
        @DecimalMin(value = "0.00", message = "Price must be non-negative")
        BigDecimal price,
    @DecimalMin(value = "0.00", message = "Cost must be non-negative")
        BigDecimal cost,
    String barcode,
    BigDecimal weight,
    BigDecimal volume,
    String description,
    UUID productTemplateId,
    Boolean isActive,
    BigDecimal minStock) {}
