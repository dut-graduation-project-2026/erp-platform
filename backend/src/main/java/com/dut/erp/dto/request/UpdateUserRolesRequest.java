package com.dut.erp.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record UpdateUserRolesRequest(
    @NotNull UUID organizationId,
    @NotNull List<UUID> roleIds
) {}
