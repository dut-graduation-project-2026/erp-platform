package com.dut.erp.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record CreateStockInventoryRequest(
    @NotBlank(message = "Inventory name cannot be blank")
    String name,
    UUID locationId
) {}
