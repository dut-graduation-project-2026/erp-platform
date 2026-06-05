package com.dut.erp.mapper;

import com.dut.erp.dto.response.ProductBaseResponse;
import com.dut.erp.dto.response.ProductResponse;
import com.dut.erp.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {OrganizationMapper.class})
public interface ProductMapper {
  ProductBaseResponse toBaseResponse(Product entity);

  ProductResponse toResponse(Product entity);
}
