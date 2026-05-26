package com.dut.erp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public record CreateStockLotRequest(
    @NotNull(message = "Product ID is required")
        UUID productId,
    @NotBlank(message = "Lot number cannot be blank")
        String lotNumber,
    Instant expirationDate) {}
