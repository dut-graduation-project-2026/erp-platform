package com.dut.erp.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public record CreateInvoiceRequest(
    @NotNull(message = "Order ID cannot be null") UUID orderId, Instant dueDate) {}
