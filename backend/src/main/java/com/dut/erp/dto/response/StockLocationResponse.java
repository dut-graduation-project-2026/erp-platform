package com.dut.erp.dto.response;

import com.dut.erp.enums.LocationType;
import java.util.UUID;

public record StockLocationResponse(
    UUID id,
    String code,
    String name,
    LocationType locationType,
    Boolean isActive,
    WarehouseBaseResponse warehouse,
    StockLocationBaseResponse parent) {}
