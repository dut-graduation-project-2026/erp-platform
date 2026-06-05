package com.dut.erp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpsertProductRequest(
    @NotBlank(message = "Name cannot be blank")
        @Size(max = 255, message = "Name cannot exceed 255 characters")
        String name,
    String description,
    @NotNull(message = "Price cannot be null")
        @Pattern(
            regexp = "^\\d+(\\.\\d{1,2})?$",
            message = "Price must be a valid decimal number with up to 2 decimal places")
        String price) {}
