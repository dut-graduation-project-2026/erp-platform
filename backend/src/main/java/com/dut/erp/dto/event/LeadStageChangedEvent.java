package com.dut.erp.dto.event;

import java.util.UUID;

public record LeadStageChangedEvent(
    UUID leadId,
    String stage
) {}
