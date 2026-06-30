package com.dut.erp.dto.request;

import com.dut.erp.enums.DocumentType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CreateInventoryDocumentRequest(
    @NotNull(message = "Document type is required")
    DocumentType documentType,

    UUID transferSourceWarehouseId,

    UUID replenishmentRequestId,

    Instant scheduledDate,

    String notes,

    @NotEmpty(message = "Items list cannot be empty")
    @Valid
    List<InventoryDocumentItemRequest> items
) {}
