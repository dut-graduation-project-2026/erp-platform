package com.dut.erp.mapper;

import com.dut.erp.dto.response.ProductTemplateBaseResponse;
import com.dut.erp.dto.response.ProductTemplateResponse;
import com.dut.erp.entity.ProductTemplate;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {OrganizationMapper.class})
public interface ProductTemplateMapper {

  ProductTemplateBaseResponse toBaseResponse(ProductTemplate entity);

  ProductTemplateResponse toResponse(ProductTemplate entity);
}
