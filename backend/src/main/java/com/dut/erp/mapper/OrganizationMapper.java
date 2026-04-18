package com.dut.erp.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.dut.erp.dto.response.OrganizationBaseResponse;
import com.dut.erp.entity.Organization;

@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OrganizationMapper {
    OrganizationBaseResponse toOrganizationBaseResponse(Organization entity);
}
