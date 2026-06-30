package com.dut.erp.dto.event;

import java.util.UUID;

public record LeadAssignedEvent(
    UUID leadId,
    UUID assigneeId
) {}
