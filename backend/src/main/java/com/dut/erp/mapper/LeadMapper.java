package com.dut.erp.mapper;

import com.dut.erp.dto.response.LeadBaseResponse;
import com.dut.erp.dto.response.LeadResponse;
import com.dut.erp.entity.Lead;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {UserMapper.class, SaleTeamMapper.class, PartnerMapper.class})
public interface LeadMapper {

  @Mapping(source = "organization.id", target = "organizationId")
  LeadResponse toResponse(Lead entity);

  LeadBaseResponse toBaseResponse(Lead entity);
}
