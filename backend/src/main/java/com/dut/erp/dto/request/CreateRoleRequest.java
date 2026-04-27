package com.dut.erp.dto.request;

import com.dut.erp.dto.common.PermissionActionPair;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import java.util.UUID;

public record CreateRoleRequest(
    @NotBlank(message = "Role name must not be blank") String name,
    @NotBlank(message = "Organization ID must not be blank") UUID organizationId,
    @NotNull(message = "Permissions must not be null") Set<PermissionActionPair> permissions) {}
