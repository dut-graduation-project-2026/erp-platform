package com.dut.erp.dto.response;

import java.util.UUID;

public record BarcodeScanResponse(
    String type, // PRODUCT, LOCATION, LOT
    UUID entityId,
    String code,
    Object details
) {}
