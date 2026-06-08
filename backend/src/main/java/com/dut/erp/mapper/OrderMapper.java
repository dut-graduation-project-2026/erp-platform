package com.dut.erp.mapper;

import com.dut.erp.dto.response.OrderBaseResponse;
import com.dut.erp.dto.response.OrderResponse;
import com.dut.erp.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {OrganizationMapper.class, PartnerMapper.class, LeadMapper.class, OrderItemMapper.class, UserMapper.class})
public interface OrderMapper {
  OrderBaseResponse toBaseResponse(Order entity);

  OrderResponse toResponse(Order entity);
}
