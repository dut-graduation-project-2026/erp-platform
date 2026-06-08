package com.dut.erp.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SaleTeamResponse(
    UUID id,
    String name,
    boolean isArchived,
    UserBaseResponse leader,
    OrganizationBaseResponse organization,
    Set<UserBaseResponse> members,
    Instant createdAt,
    Instant updatedAt,
    UserBaseResponse createdBy,
    UserBaseResponse updatedBy) {}
