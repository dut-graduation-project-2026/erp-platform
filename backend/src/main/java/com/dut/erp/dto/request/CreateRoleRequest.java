package com.dut.erp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Set;
import java.util.UUID;

public record CreateRoleRequest(
    @NotBlank(message = "Role name cannot be blank")
        @Size(max = 50, message = "Role name must not exceed 50 characters")
        String name,
    Set<UUID> permissionIds) {}
