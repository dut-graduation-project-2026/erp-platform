package com.dut.erp.dto.response;

import com.dut.erp.enums.InvoiceStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SaleInvoiceResponse(
    UUID id,
    String invoiceNumber,
    Instant invoiceDate,
    Instant dueDate,
    BigDecimal totalAmount,
    BigDecimal paidAmount,
    InvoiceStatus status,
    SalePartnerBaseResponse partner,
    SaleOrderBaseResponse order) {}
