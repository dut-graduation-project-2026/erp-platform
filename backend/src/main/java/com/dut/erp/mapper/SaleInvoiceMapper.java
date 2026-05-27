package com.dut.erp.mapper;

import com.dut.erp.dto.response.SaleInvoiceBaseResponse;
import com.dut.erp.dto.response.SaleInvoiceResponse;
import com.dut.erp.entity.SaleInvoice;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {SalePartnerMapper.class, SaleOrderMapper.class})
public interface SaleInvoiceMapper {

  SaleInvoiceBaseResponse toBaseResponse(SaleInvoice entity);

  SaleInvoiceResponse toResponse(SaleInvoice entity);
}
