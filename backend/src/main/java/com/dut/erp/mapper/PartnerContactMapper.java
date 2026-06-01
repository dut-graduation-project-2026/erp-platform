package com.dut.erp.mapper;

import com.dut.erp.dto.response.PartnerContactResponse;
import com.dut.erp.entity.PartnerContact;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PartnerContactMapper {
  PartnerContactResponse toPartnerContactResponse(PartnerContact entity);
}
