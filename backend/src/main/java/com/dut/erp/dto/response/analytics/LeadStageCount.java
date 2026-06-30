package com.dut.erp.dto.response.analytics;

import com.dut.erp.enums.LeadStage;

public record LeadStageCount(LeadStage stage, long count) {}
