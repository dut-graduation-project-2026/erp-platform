package com.dut.erp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProductRequest(
    @NotBlank(message = "SKU cannot be blank")
        @Size(max = 100, message = "SKU cannot exceed 100 characters")
        String sku,
    @NotBlank(message = "Name cannot be blank")
        @Size(max = 255, message = "Name cannot exceed 255 characters")
        String name,
    String description,
    @NotBlank(message = "Price cannot be blank")
        @Pattern(
            regexp = "^\\d+(\\.\\d{1,2})?$",
            message = "Price must be a valid decimal number with up to 2 decimal places")
        String price) {}
