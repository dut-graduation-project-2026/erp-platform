package com.dut.erp.mapper;

import com.dut.erp.dto.response.PermissionResponse;
import com.dut.erp.entity.Permission;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PermissionMapper {
  PermissionResponse toPermissionResponse(Permission entity);
}
