package com.dut.erp.dto.event;

import com.dut.erp.enums.OrderStatus;
import java.util.UUID;

public record OrderStatusChangedEvent(
    UUID orderId,
    OrderStatus oldStatus,
    OrderStatus newStatus
) {}
