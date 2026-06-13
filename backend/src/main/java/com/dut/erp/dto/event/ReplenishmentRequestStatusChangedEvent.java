package com.dut.erp.dto.event;

import com.dut.erp.enums.ReplenishmentStatus;
import java.util.UUID;

public record ReplenishmentRequestStatusChangedEvent(
    UUID replenishmentId,
    ReplenishmentStatus oldStatus,
    ReplenishmentStatus newStatus
) {}
