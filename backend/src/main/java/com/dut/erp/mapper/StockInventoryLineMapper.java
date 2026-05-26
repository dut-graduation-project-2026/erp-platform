package com.dut.erp.mapper;

import com.dut.erp.dto.response.StockInventoryLineResponse;
import com.dut.erp.entity.StockInventoryLine;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {ProductMapper.class, StockLocationMapper.class, StockLotMapper.class})
public interface StockInventoryLineMapper {

  @Mapping(target = "variance", expression = "java(entity.getCheckedQty().subtract(entity.getTheoreticalQty()))")
  StockInventoryLineResponse toResponse(StockInventoryLine entity);
}
