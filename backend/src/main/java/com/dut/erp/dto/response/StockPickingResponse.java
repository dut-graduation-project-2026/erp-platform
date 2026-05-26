package com.dut.erp.dto.response;

import com.dut.erp.enums.PickingType;
import com.dut.erp.enums.StockPickingState;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record StockPickingResponse(
    UUID id,
    String name,
    PickingType pickingType,
    StockLocationBaseResponse location,
    StockLocationBaseResponse locationDest,
    SalePartnerResponse partner,
    UUID saleOrderId,
    UUID purchaseOrderId,
    StockPickingState state,
    Instant scheduledDate,
    Instant dateDone,
    List<StockMoveResponse> stockMoves) {}
