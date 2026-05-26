package com.dut.erp.mapper;

import com.dut.erp.dto.response.StockQuantResponse;
import com.dut.erp.entity.StockQuant;
import com.dut.erp.enums.CostMethod;
import java.math.BigDecimal;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface StockQuantMapper {

  default StockQuantResponse toResponse(StockQuant entity) {
    if (entity == null) {
      return null;
    }

    UUID id = entity.getId();
    StockQuantResponse.ProductMinResponse product = null;
    if (entity.getProduct() != null) {
      product = new StockQuantResponse.ProductMinResponse(
          entity.getProduct().getId(),
          entity.getProduct().getSku(),
          entity.getProduct().getName()
      );
    }

    StockQuantResponse.WarehouseMinResponse warehouse = null;
    if (entity.getLocation() != null && entity.getLocation().getWarehouse() != null) {
      warehouse = new StockQuantResponse.WarehouseMinResponse(
          entity.getLocation().getWarehouse().getId(),
          entity.getLocation().getWarehouse().getCode()
      );
    }

    StockQuantResponse.LocationMinResponse location = null;
    if (entity.getLocation() != null) {
      location = new StockQuantResponse.LocationMinResponse(
          entity.getLocation().getId(),
          entity.getLocation().getCode()
      );
    }

    StockQuantResponse.LotMinResponse lot = null;
    if (entity.getLot() != null) {
      lot = new StockQuantResponse.LotMinResponse(
          entity.getLot().getId(),
          entity.getLot().getLotNumber(),
          entity.getLot().getExpirationDate() != null ? entity.getLot().getExpirationDate().toString() : null
      );
    }

    BigDecimal quantity = entity.getQuantity() != null ? entity.getQuantity() : BigDecimal.ZERO;
    BigDecimal reservedQuantity = entity.getReservedQuantity() != null ? entity.getReservedQuantity() : BigDecimal.ZERO;
    BigDecimal availableQuantity = quantity.subtract(reservedQuantity);

    BigDecimal unitCost = BigDecimal.ZERO;
    CostMethod costMethod = CostMethod.FIFO;
    if (entity.getProduct() != null) {
      unitCost = entity.getProduct().getCost() != null ? entity.getProduct().getCost() : BigDecimal.ZERO;
      if (entity.getProduct().getProductTemplate() != null) {
        costMethod = entity.getProduct().getProductTemplate().getValuationMethod();
      }
    }
    BigDecimal totalValue = quantity.multiply(unitCost);

    return new StockQuantResponse(
        id,
        product,
        warehouse,
        location,
        lot,
        quantity,
        reservedQuantity,
        availableQuantity,
        unitCost,
        totalValue,
        costMethod
    );
  }
}
