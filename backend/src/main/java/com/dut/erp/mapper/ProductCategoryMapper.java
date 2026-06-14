package com.dut.erp.mapper;

import com.dut.erp.dto.response.ProductCategoryBaseResponse;
import com.dut.erp.dto.response.ProductCategoryResponse;
import com.dut.erp.entity.ProductCategory;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {OrganizationMapper.class, UserMapper.class})
public interface ProductCategoryMapper {
  ProductCategoryBaseResponse toBaseResponse(ProductCategory entity);

  ProductCategoryResponse toResponse(ProductCategory entity);
}
