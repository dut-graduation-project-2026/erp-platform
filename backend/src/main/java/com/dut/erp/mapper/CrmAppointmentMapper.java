package com.dut.erp.mapper;

import com.dut.erp.dto.response.CrmAppointmentResponse;
import com.dut.erp.entity.CrmAppointment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CrmAppointmentMapper {

  @Mapping(target = "leadId", source = "lead.id")
  CrmAppointmentResponse toResponse(CrmAppointment entity);
}
