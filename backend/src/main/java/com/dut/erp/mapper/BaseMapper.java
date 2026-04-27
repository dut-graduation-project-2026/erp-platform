package com.dut.erp.mapper;

import com.dut.erp.dto.response.OrganizationBaseResponse;
import com.dut.erp.dto.response.UserBaseResponse;
import com.dut.erp.entity.Organization;
import com.dut.erp.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BaseMapper {
  UserBaseResponse toUserBaseResponse(User entity);

  OrganizationBaseResponse toOrganizationBaseResponse(Organization entity);
}
