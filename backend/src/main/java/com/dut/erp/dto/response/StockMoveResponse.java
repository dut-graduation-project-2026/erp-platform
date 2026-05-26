package com.dut.erp.dto.response;

import com.dut.erp.enums.StockMoveState;
import java.math.BigDecimal;
import java.util.UUID;

public record StockMoveResponse(
    UUID id,
    ProductBaseResponse product,
    StockLocationBaseResponse location,
    StockLocationBaseResponse locationDest,
    StockLotResponse lot,
    BigDecimal productUomQty,
    BigDecimal quantityDone,
    StockMoveState state) {}
