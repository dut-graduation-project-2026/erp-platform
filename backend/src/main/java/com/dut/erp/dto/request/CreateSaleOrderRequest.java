package com.dut.erp.dto.request;

import com.dut.erp.enums.SaleOrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CreateSaleOrderRequest(
    @NotNull(message = "Partner ID is required") UUID partnerId,
    UUID opportunityId,
    UUID salespersonId,
    @NotNull(message = "Order date is required") Instant orderDate,
    @NotEmpty(message = "Order must have at least one line item") @Valid
        List<SaleOrderLineRequest> lines,
    SaleOrderStatus status) {}
