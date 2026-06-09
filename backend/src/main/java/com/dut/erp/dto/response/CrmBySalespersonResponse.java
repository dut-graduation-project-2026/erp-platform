package com.dut.erp.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record CrmBySalespersonResponse(
    UUID salespersonId,
    String salespersonName,
    Long totalLeads,
    Long wonLeads,
    BigDecimal expectedRevenue,
    BigDecimal wonRevenue
) {}
