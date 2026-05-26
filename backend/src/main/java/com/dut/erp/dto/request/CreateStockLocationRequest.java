package com.dut.erp.dto.request;

import com.dut.erp.enums.LocationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateStockLocationRequest(
    @NotNull(message = "Warehouse ID is required")
        UUID warehouseId,
    UUID parentId,
    @NotBlank(message = "Location name cannot be blank")
        @Size(max = 255, message = "Location name must not exceed 255 characters")
        String name,
    @NotBlank(message = "Location code cannot be blank")
        @Size(max = 100, message = "Location code must not exceed 100 characters")
        String code,
    @NotNull(message = "Location type is required")
        LocationType locationType) {}
