package com.dut.erp.dto.request;

import com.dut.erp.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(
    @NotNull(message = "Status cannot be null")
    OrderStatus status
) {}
