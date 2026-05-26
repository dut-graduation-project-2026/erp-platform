package com.dut.erp.mapper;

import com.dut.erp.dto.response.PartnerContactResponse;
import com.dut.erp.dto.response.SalePartnerBaseResponse;
import com.dut.erp.dto.response.SalePartnerResponse;
import com.dut.erp.entity.PartnerContact;
import com.dut.erp.entity.SalePartner;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {OrganizationMapper.class})
public interface SalePartnerMapper {

  SalePartnerBaseResponse toBaseResponse(SalePartner entity);

  SalePartnerResponse toResponse(SalePartner entity);

  PartnerContactResponse toContactResponse(PartnerContact entity);
}
