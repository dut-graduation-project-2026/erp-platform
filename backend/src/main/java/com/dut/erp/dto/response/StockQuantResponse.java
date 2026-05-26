package com.dut.erp.dto.response;

import com.dut.erp.enums.CostMethod;
import java.math.BigDecimal;
import java.util.UUID;

public record StockQuantResponse(
    UUID id,
    ProductMinResponse product,
    WarehouseMinResponse warehouse,
    LocationMinResponse location,
    LotMinResponse lot,
    BigDecimal quantity,
    BigDecimal reservedQuantity,
    BigDecimal availableQuantity,
    BigDecimal unitCost,
    BigDecimal totalValue,
    CostMethod costMethod) {

  public record ProductMinResponse(UUID id, String sku, String name) {}
  public record WarehouseMinResponse(UUID id, String code) {}
  public record LocationMinResponse(UUID id, String code) {}
  public record LotMinResponse(UUID id, String batchNumber, String expirationDate) {}
}
