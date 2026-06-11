package com.dut.erp.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record OrganizationMemberResponse(
    UUID id,
    String email,
    String firstName,
    String lastName,
    List<RoleBaseResponse> roles,
    String status,
    String lastLogin
) {}
