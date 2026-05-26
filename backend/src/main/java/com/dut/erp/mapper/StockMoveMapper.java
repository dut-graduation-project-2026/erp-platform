package com.dut.erp.mapper;

import com.dut.erp.dto.response.StockMoveResponse;
import com.dut.erp.entity.StockMove;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {ProductMapper.class, StockLocationMapper.class, StockLotMapper.class})
public interface StockMoveMapper {

  StockMoveResponse toResponse(StockMove entity);
}
