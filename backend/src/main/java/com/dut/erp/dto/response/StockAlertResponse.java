package com.dut.erp.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record StockAlertResponse(
    String type, // "MIN_STOCK" or "EXPIRING_LOT"
    String message,
    UUID productId,
    String productSku,
    String productName,
    BigDecimal currentQuantity,
    BigDecimal thresholdValue, // minStock or null
    UUID lotId, // optional
    String lotNumber, // optional
    Instant expirationDate, // optional
    BigDecimal suggestedPurchaseQty // optional
) {}
