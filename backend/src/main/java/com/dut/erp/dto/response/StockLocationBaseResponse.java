package com.dut.erp.dto.response;

import com.dut.erp.enums.LocationType;
import java.util.UUID;

public record StockLocationBaseResponse(
    UUID id,
    String code,
    String name,
    LocationType locationType,
    Boolean isActive) {}
