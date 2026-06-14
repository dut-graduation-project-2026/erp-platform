package com.dut.erp.dto.response.analytics;

import com.dut.erp.enums.LeadStage;
import java.math.BigDecimal;

public record PipelineStageSummary(
    LeadStage stage,
    long leadCount,
    BigDecimal totalExpectedRevenue,
    BigDecimal weightedRevenue,
    double averageProbability
) {}
