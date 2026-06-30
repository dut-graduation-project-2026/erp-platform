package com.dut.erp.dto.request;

import com.dut.erp.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record UpdateOrderStatusRequest(
    @NotNull(message = "Status cannot be null")
    OrderStatus status,
    UUID warehouseId
) {}
