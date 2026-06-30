package com.dut.erp.mapper;

import com.dut.erp.dto.response.WarehouseBaseResponse;
import com.dut.erp.dto.response.WarehouseResponse;
import com.dut.erp.entity.Warehouse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {OrganizationMapper.class, UserMapper.class})
public interface WarehouseMapper {
  @Mapping(target = "staffCount", expression = "java(entity.getStaff() != null ? entity.getStaff().size() : 0)")
  WarehouseBaseResponse toBaseResponse(Warehouse entity);

  WarehouseResponse toResponse(Warehouse entity);
}
