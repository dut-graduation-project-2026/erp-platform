package com.dut.erp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateWarehouseRequest(
    @NotBlank(message = "Warehouse name cannot be blank")
        @Size(max = 255, message = "Warehouse name must not exceed 255 characters")
        String name,
    @NotBlank(message = "Warehouse code cannot be blank")
        @Size(max = 50, message = "Warehouse code must not exceed 50 characters")
        String code,
    @Size(max = 255, message = "Address must not exceed 255 characters")
        String address) {}
