package com.dut.erp.mapper;

import com.dut.erp.dto.response.OrderItemResponse;
import com.dut.erp.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {ProductMapper.class, TaxMapper.class})
public interface OrderItemMapper {
  @Mapping(target = "organizationId", source = "organization.id")
  @Mapping(target = "orderId", source = "order.id")
  OrderItemResponse toResponse(OrderItem entity);
}
