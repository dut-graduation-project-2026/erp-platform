package com.dut.erp.mapper;

import com.dut.erp.dto.response.TaxBaseResponse;
import com.dut.erp.dto.response.TaxResponse;
import com.dut.erp.entity.Tax;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {OrganizationMapper.class, UserMapper.class})
public interface TaxMapper {
  TaxBaseResponse toBaseResponse(Tax entity);

  TaxResponse toResponse(Tax entity);
}
