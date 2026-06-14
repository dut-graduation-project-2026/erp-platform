package com.dut.erp.dto.event;

import com.dut.erp.enums.InvoiceStatus;
import java.util.UUID;

public record InvoiceStatusChangedEvent(
    UUID invoiceId,
    InvoiceStatus oldStatus,
    InvoiceStatus newStatus
) {}
