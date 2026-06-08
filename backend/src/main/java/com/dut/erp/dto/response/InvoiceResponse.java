package com.dut.erp.dto.response;

import com.dut.erp.enums.InvoiceStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record InvoiceResponse(
    UUID id,
    OrganizationBaseResponse organization,
    OrderBaseResponse order,
    PartnerBaseResponse partner,
    String invoiceNumber,
    Instant dueDate,
    BigDecimal totalAmount,
    BigDecimal paidAmount,
    InvoiceStatus status,
    Instant createdAt,
    Instant updatedAt,
    UserBaseResponse createdBy,
    UserBaseResponse updatedBy
) {}
