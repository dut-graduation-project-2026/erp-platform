package com.dut.erp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateWarehouseRequest(
    @NotBlank(message = "Warehouse name cannot be blank")
        @Size(max = 255, message = "Warehouse name must not exceed 255 characters")
        String name,
    @Size(max = 255, message = "Address must not exceed 255 characters")
        String address,
    Boolean isActive) {}
