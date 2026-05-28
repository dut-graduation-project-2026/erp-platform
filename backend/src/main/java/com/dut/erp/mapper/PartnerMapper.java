package com.dut.erp.mapper;

import com.dut.erp.dto.response.PartnerResponse;
import com.dut.erp.entity.Partner;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {UserMapper.class, PartnerContactMapper.class})
public interface PartnerMapper {

  @Mapping(source = "organization.id", target = "organizationId")
  PartnerResponse toPartnerResponse(Partner entity);
}
