package com.dut.erp.mapper;

import com.dut.erp.dto.response.StockInventoryBaseResponse;
import com.dut.erp.dto.response.StockInventoryResponse;
import com.dut.erp.entity.StockInventory;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {StockLocationMapper.class, OrganizationMapper.class, StockInventoryLineMapper.class})
public interface StockInventoryMapper {

  StockInventoryBaseResponse toBaseResponse(StockInventory entity);

  StockInventoryResponse toResponse(StockInventory entity);
}
