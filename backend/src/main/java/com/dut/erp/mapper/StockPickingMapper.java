package com.dut.erp.mapper;

import com.dut.erp.dto.response.StockPickingBaseResponse;
import com.dut.erp.dto.response.StockPickingResponse;
import com.dut.erp.entity.StockPicking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {StockLocationMapper.class, SalePartnerMapper.class, StockMoveMapper.class})
public interface StockPickingMapper {

  StockPickingBaseResponse toBaseResponse(StockPicking entity);

  @Mapping(target = "saleOrderId", source = "saleOrder.id")
  StockPickingResponse toResponse(StockPicking entity);
}
