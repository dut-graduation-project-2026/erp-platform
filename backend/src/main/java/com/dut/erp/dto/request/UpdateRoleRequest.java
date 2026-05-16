package com.dut.erp.dto.request;

import java.util.Set;
import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateRoleRequest(
    @NotBlank(message = "Role name cannot be blank")
        @Size(max = 50, message = "Role name must not exceed 50 characters")
        String name,
    Set<UUID> permissionIds) {}
