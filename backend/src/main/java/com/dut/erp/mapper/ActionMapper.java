package com.dut.erp.mapper;

import com.dut.erp.dto.response.ActionBaseResponse;
import com.dut.erp.entity.Action;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ActionMapper {
  ActionBaseResponse toActionBaseResponse(Action entity);
}
