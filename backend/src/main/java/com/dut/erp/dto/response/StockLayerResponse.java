package com.dut.erp.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record StockLayerResponse(
    UUID documentLineId,
    String documentName,
    Instant dateDone,
    BigDecimal originalQuantity,
    BigDecimal remainingQuantity,
    BigDecimal unitCost
) {}
