package com.dut.erp.dto.request;

import com.dut.erp.enums.InvoiceStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateInvoiceStatusRequest(
    @NotNull(message = "Status cannot be null")
    InvoiceStatus status
) {}
