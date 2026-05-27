package com.dut.erp.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.Set;
import java.util.UUID;

public record UpdateOrganizationMemberRolesRequest(
    @NotNull(message = "Role IDs cannot be null") Set<UUID> roleIds
) {}
