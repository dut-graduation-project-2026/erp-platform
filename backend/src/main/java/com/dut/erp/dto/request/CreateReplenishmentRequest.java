package com.dut.erp.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateReplenishmentRequest(
    @NotNull(message = "Inventory Document ID is required")
    UUID inventoryDocumentId,

    String notes
) {}
