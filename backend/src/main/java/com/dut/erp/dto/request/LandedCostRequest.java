package com.dut.erp.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record LandedCostRequest(
    @NotNull(message = "Landed cost amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    BigDecimal amount,
    @NotBlank(message = "Allocation method is required")
    String allocationMethod // EQUAL, BY_QUANTITY
) {}
