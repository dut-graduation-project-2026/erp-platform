package com.dut.erp.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record StockInventoryLineResponse(
    UUID id,
    ProductBaseResponse product,
    StockLocationBaseResponse location,
    StockLotResponse lot,
    BigDecimal theoreticalQty,
    BigDecimal checkedQty,
    BigDecimal variance) {}
