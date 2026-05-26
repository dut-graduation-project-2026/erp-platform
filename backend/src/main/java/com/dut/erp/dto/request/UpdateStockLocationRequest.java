package com.dut.erp.dto.request;

import com.dut.erp.enums.LocationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateStockLocationRequest(
    @NotBlank(message = "Location name cannot be blank")
        @Size(max = 255, message = "Location name must not exceed 255 characters")
        String name,
    @NotNull(message = "Location type is required")
        LocationType locationType,
    Boolean isActive) {}
