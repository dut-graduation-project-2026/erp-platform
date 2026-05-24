package com.dut.erp.mapper;

import com.dut.erp.dto.response.RoleBaseResponse;
import com.dut.erp.dto.response.RoleResponse;
import com.dut.erp.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {OrganizationMapper.class, PermissionMapper.class, UserMapper.class})
public interface RoleMapper {
  RoleBaseResponse toRoleBaseResponse(Role entity);

  RoleResponse toRoleResponse(Role entity);
}
