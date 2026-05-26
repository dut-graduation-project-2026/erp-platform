package com.dut.erp.dto.response;

import java.time.Instant;
import java.util.UUID;

public record StockLotResponse(
    UUID id,
    String lotNumber,
    Instant expirationDate,
    ProductBaseResponse product) {}
