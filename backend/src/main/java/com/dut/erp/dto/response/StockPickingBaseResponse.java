package com.dut.erp.dto.response;

import com.dut.erp.enums.PickingType;
import com.dut.erp.enums.StockPickingState;
import java.time.Instant;
import java.util.UUID;

public record StockPickingBaseResponse(
    UUID id,
    String name,
    PickingType pickingType,
    StockPickingState state,
    Instant scheduledDate,
    Instant dateDone) {}
