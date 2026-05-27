package com.dut.erp.dto.response;

import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record OrganizationMemberResponse(
    UUID id,
    String email,
    String firstName,
    String lastName,
    List<RoleResponse> roles,
    String status,
    String lastLogin
) {}
