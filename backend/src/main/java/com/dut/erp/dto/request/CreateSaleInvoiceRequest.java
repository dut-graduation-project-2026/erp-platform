package com.dut.erp.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public record CreateSaleInvoiceRequest(
    @NotNull(message = "Order ID is required") UUID orderId,
    @NotNull(message = "Invoice date is required") Instant invoiceDate,
    @NotNull(message = "Due date is required") Instant dueDate) {}
