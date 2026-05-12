package com.dut.erp.dto.response;

import java.util.Set;
import java.util.UUID;

public record RoleResponse(
    UUID id,
    String name,
    Set<PermissionResponse> permissions,
    OrganizationBaseResponse organization) {}
