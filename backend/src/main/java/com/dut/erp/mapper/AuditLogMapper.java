package com.dut.erp.mapper;

import com.dut.erp.dto.response.AuditLogResponse;
import com.dut.erp.entity.AuditLog;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {UserMapper.class})
public interface AuditLogMapper {
  AuditLogResponse toAuditLogResponse(AuditLog entity);
}
