package com.dut.erp.mapper;

import com.dut.erp.dto.response.InventoryBalanceBaseResponse;
import com.dut.erp.dto.response.InventoryBalanceResponse;
import com.dut.erp.entity.InventoryBalance;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {WarehouseMapper.class, ProductMapper.class, UserMapper.class})
public interface InventoryBalanceMapper {

  InventoryBalanceBaseResponse toBaseResponse(InventoryBalance entity);

  InventoryBalanceResponse toResponse(InventoryBalance entity);
}
