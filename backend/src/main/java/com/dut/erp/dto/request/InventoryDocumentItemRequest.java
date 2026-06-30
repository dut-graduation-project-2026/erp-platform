package com.dut.erp.dto.request;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record InventoryDocumentItemRequest(
    @NotNull(message = "Product ID is required") UUID productId,
    @NotNull(message = "Quantity is required") BigDecimal quantity) {}
