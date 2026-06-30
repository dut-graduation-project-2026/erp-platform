package com.dut.erp.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

public record UpsertOrderRequest(
    @NotNull(message = "Lead ID cannot be null") UUID leadId,
    /** Order number provided by the user. If null or blank, the backend will auto-generate one. */
    @Size(max = 100, message = "Order number cannot exceed 100 characters") String orderNumber,
    UUID warehouseId,
    Instant deliveryDate,
    Instant expirationDate) {}
