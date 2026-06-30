package com.dut.erp.mapper;

import com.dut.erp.dto.response.OrderBaseResponse;
import com.dut.erp.dto.response.OrderResponse;
import com.dut.erp.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {OrganizationMapper.class, PartnerMapper.class, LeadMapper.class, OrderItemMapper.class, UserMapper.class})
public interface OrderMapper {
  @Mapping(target = "warehouseId", ignore = true)
  @Mapping(target = "warehouseName", ignore = true)
  @Mapping(source = "lead.saleTeam.id", target = "saleTeamId")
  @Mapping(source = "lead.saleTeam.name", target = "saleTeamName")
  OrderBaseResponse toBaseResponse(Order entity);

  @Mapping(target = "warehouseId", ignore = true)
  @Mapping(target = "warehouseName", ignore = true)
  @Mapping(target = "invoiceId", ignore = true)
  @Mapping(target = "invoiceNumber", ignore = true)
  @Mapping(target = "invoiceStatus", ignore = true)
  @Mapping(source = "lead.saleTeam.id", target = "saleTeamId")
  @Mapping(source = "lead.saleTeam.name", target = "saleTeamName")
  OrderResponse toResponse(Order entity);
}
