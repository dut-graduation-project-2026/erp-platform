package com.dut.erp.dto.response;

import java.math.BigDecimal;

public record CrmStageDistributionResponse(
    String stage,
    Long leadCount,
    BigDecimal expectedRevenue
) {}
