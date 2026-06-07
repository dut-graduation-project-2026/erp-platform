package com.dut.erp.dto.response;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record SaleTeamResponse(
    UUID id,
    String name,
    Boolean isArchived,
    UserBaseResponse leader,
    OrganizationBaseResponse organization,
    Set<UserBaseResponse> members,
    Instant createdAt,
    Instant updatedAt,
    UserBaseResponse createdBy,
    UserBaseResponse updatedBy) {}
