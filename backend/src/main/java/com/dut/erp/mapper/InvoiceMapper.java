package com.dut.erp.mapper;

import com.dut.erp.dto.response.InvoiceBaseResponse;
import com.dut.erp.dto.response.InvoiceResponse;
import com.dut.erp.entity.Invoice;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {OrganizationMapper.class, OrderMapper.class, PartnerMapper.class, UserMapper.class})
public interface InvoiceMapper {
  InvoiceBaseResponse toBaseResponse(Invoice entity);

  InvoiceResponse toResponse(Invoice entity);
}
