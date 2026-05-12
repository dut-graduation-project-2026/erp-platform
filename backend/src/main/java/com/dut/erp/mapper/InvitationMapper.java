package com.dut.erp.mapper;

import com.dut.erp.dto.response.OrganizationInvitationResponse;
import com.dut.erp.entity.OrganizationInvitation;

import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface InvitationMapper {
  OrganizationInvitationResponse toOrganizationInvitationResponse(
      OrganizationInvitation entity);
}
