package com.dut.erp.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

public record UpsertProductRequest(
    @NotBlank(message = "Name cannot be blank")
        @Size(max = 255, message = "Name cannot exceed 255 characters")
        String name,
    @NotBlank(message = "SKU cannot be blank")
        @Size(max = 100, message = "SKU cannot exceed 100 characters")
        String sku,
    String description,
    @NotNull(message = "Price cannot be null")
        @DecimalMin(value = "0.00", message = "Price must be greater than or equal to 0.00")
        @Digits(
            integer = 13,
            fraction = 2,
            message =
                "Price must be a valid decimal number with up to 13 integer digits and 2 decimal"
                    + " places")
        BigDecimal price,
    @NotNull(message = "Product category ID is required")
        UUID categoryId,
    String image) {}
