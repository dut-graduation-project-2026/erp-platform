package com.dut.erp.mapper;

import com.dut.erp.dto.response.StockLotResponse;
import com.dut.erp.entity.StockLot;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {ProductMapper.class})
public interface StockLotMapper {

  StockLotResponse toResponse(StockLot entity);
}
