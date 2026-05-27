package com.dut.erp.mapper;

import com.dut.erp.dto.response.CrmLeadBaseResponse;
import com.dut.erp.dto.response.CrmLeadResponse;
import com.dut.erp.entity.CrmLead;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {SalePartnerMapper.class, UserMapper.class})
public interface CrmLeadMapper {

  @Mapping(target = "stage", source = "stage")
  CrmLeadBaseResponse toBaseResponse(CrmLead entity);

  @Mapping(target = "stage", source = "stage")
  @Mapping(target = "partner", source = "partner")
  @Mapping(target = "salesperson", source = "salesperson")
  @Mapping(target = "salesTeam", source = "salesTeam")
  CrmLeadResponse toResponse(CrmLead entity);
}
