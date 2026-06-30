package com.dut.erp.mapper;

import com.dut.erp.dto.response.ProductBaseResponse;
import com.dut.erp.dto.response.ProductResponse;
import com.dut.erp.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {OrganizationMapper.class, UserMapper.class, ProductCategoryMapper.class})
public interface ProductMapper {
  @Mapping(source = "archived", target = "isArchived")
  ProductBaseResponse toBaseResponse(Product entity);

  @Mapping(source = "archived", target = "isArchived")
  ProductResponse toResponse(Product entity);
}
