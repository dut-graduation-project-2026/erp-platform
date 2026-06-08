package com.dut.erp.dto.request;

import com.dut.erp.annotation.ValueOfEnum;
import com.dut.erp.enums.InvoiceStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record UpdateInvoiceStatusRequest(
    @Pattern(regexp = "^\\S+$", message = "Invoice status must not contain whitespace")
        @NotNull(message = "Invoice status cannot be null")
        @ValueOfEnum(
            enumClass = InvoiceStatus.class,
            message = "Invoice status must be one of: {enumValues}")
        String status) {}
