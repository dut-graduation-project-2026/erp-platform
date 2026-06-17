package com.dut.erp.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ReplenishmentRequestResponse(
    UUID id,
    UUID warehouseId,
    String warehouseName,
    UUID inventoryDocumentId,
    String inventoryDocumentName,
    String notes,
    String status,
    Instant createdAt,
    UserBaseResponse createdBy,
    String orderNumber,
    UUID referenceId
) {}
