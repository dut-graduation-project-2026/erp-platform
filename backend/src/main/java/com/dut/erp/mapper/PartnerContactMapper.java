package com.dut.erp.mapper;

import com.dut.erp.dto.response.PartnerContactResponse;
import com.dut.erp.entity.PartnerContact;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PartnerContactMapper {

  @Mapping(source = "partner.id", target = "partnerId")
  PartnerContactResponse toPartnerContactResponse(PartnerContact entity);
}
