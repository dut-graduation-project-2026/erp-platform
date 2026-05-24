package com.dut.erp.mapper;

import com.dut.erp.dto.response.UserBaseResponse;
import com.dut.erp.dto.response.UserResponse;
import com.dut.erp.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {OrganizationMapper.class})
public interface UserMapper {
  UserBaseResponse toUserBaseResponse(User entity);

  UserResponse toUserResponse(User entity);
}
