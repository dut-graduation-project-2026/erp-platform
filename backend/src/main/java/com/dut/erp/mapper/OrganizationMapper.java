package com.dut.erp.mapper;

import com.dut.erp.dto.response.OrganizationResponse;
import com.dut.erp.entity.Organization;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OrganizationMapper {
  OrganizationResponse toOrganizationResponse(Organization entity);
}
