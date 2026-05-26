package com.dut.erp.mapper;

import com.dut.erp.dto.response.StockLocationBaseResponse;
import com.dut.erp.dto.response.StockLocationResponse;
import com.dut.erp.entity.StockLocation;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {WarehouseMapper.class})
public interface StockLocationMapper {

  StockLocationBaseResponse toBaseResponse(StockLocation entity);

  StockLocationResponse toResponse(StockLocation entity);
}
