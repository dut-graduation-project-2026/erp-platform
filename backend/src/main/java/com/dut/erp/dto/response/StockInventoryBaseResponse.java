package com.dut.erp.dto.response;

import com.dut.erp.enums.StockInventoryState;
import java.time.Instant;
import java.util.UUID;

public record StockInventoryBaseResponse(
    UUID id,
    String name,
    StockLocationBaseResponse location,
    StockInventoryState state,
    Instant inventoryDate) {}
