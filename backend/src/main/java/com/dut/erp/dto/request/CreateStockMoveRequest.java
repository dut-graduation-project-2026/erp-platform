package com.dut.erp.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateStockMoveRequest(
    @NotNull(message = "Product ID is required")
        UUID productId,
    @NotNull(message = "Quantity is required")
        @DecimalMin(value = "0.0001", message = "Quantity must be positive")
        BigDecimal productUomQty,
    UUID lotId) {}
