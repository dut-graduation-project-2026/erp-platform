package com.dut.erp.mapper;

import com.dut.erp.dto.response.SaleOrderBaseResponse;
import com.dut.erp.dto.response.SaleOrderLineResponse;
import com.dut.erp.dto.response.SaleOrderResponse;
import com.dut.erp.entity.SaleOrder;
import com.dut.erp.entity.SaleOrderLine;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {SalePartnerMapper.class, ProductMapper.class, UserMapper.class})
public interface SaleOrderMapper {

  SaleOrderBaseResponse toBaseResponse(SaleOrder entity);

  SaleOrderResponse toResponse(SaleOrder entity);

  SaleOrderLineResponse toLineResponse(SaleOrderLine entity);
}
