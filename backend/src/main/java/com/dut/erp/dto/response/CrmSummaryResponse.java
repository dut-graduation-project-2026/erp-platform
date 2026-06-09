package com.dut.erp.dto.response;

import java.math.BigDecimal;

public record CrmSummaryResponse(
    Long totalLeads,
    Long wonLeads,
    Long lostLeads,
    BigDecimal conversionRate,
    BigDecimal expectedRevenue,
    BigDecimal wonRevenue
) {}
