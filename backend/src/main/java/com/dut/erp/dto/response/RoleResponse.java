package com.dut.erp.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record RoleResponse(
    UUID id,
    String name,
    OrganizationBaseResponse organization,
    Set<PermissionBaseResponse> permissions,
    Instant createdAt,
    Instant updatedAt,
    UserBaseResponse createdBy,
    UserBaseResponse updatedBy) {}
