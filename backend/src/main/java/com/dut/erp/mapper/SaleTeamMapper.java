package com.dut.erp.mapper;

import com.dut.erp.dto.response.SaleTeamBaseResponse;
import com.dut.erp.dto.response.SaleTeamResponse;
import com.dut.erp.entity.SaleTeam;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {OrganizationMapper.class, UserMapper.class})
public interface SaleTeamMapper {
  SaleTeamBaseResponse toBaseResponse(SaleTeam entity);

  SaleTeamResponse toResponse(SaleTeam entity);
}
