package com.dut.erp.dto.response;

import com.dut.erp.enums.StockInventoryState;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record StockInventoryResponse(
    UUID id,
    String name,
    StockLocationBaseResponse location,
    StockInventoryState state,
    Instant inventoryDate,
    OrganizationBaseResponse organization,
    List<StockInventoryLineResponse> inventoryLines) {}
