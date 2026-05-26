package com.dut.erp.dto.request;

import com.dut.erp.enums.PickingType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CreateStockPickingRequest(
    @NotNull(message = "Picking type is required")
        PickingType pickingType,
    @NotNull(message = "Source location is required")
        UUID locationId,
    @NotNull(message = "Destination location is required")
        UUID locationDestId,
    UUID partnerId,
    UUID saleOrderId,
    UUID purchaseOrderId,
    Instant scheduledDate,
    @NotEmpty(message = "Stock moves cannot be empty")
        @Valid
        List<CreateStockMoveRequest> stockMoves) {}
