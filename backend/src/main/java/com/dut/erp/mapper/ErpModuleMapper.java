package com.dut.erp.mapper;

import com.dut.erp.dto.response.ErpModuleBaseResponse;
import com.dut.erp.dto.response.ErpModuleResponse;
import com.dut.erp.entity.ErpModule;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {PermissionMapper.class})
public interface ErpModuleMapper {
  ErpModuleBaseResponse toErpModuleBaseResponse(ErpModule entity);

  ErpModuleResponse toErpModuleResponse(ErpModule entity);
}
