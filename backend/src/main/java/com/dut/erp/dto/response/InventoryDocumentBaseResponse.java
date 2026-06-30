package com.dut.erp.dto.response;

import com.dut.erp.enums.DocumentStatus;
import com.dut.erp.enums.DocumentType;
import com.dut.erp.enums.ReferenceType;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record InventoryDocumentBaseResponse(
    UUID id,
    UUID warehouseId,
    String warehouseName,
    UUID sourceWarehouseId,
    String sourceWarehouseName,
    String name,
    DocumentType documentType,
    ReferenceType referenceType,
    UUID referenceId,
    String orderNumber,
    String partnerName,
    String deliveryAddress,
    DocumentStatus documentStatus,
    Instant scheduledDate,
    Instant dateDone,
    Instant createdAt
) {}
