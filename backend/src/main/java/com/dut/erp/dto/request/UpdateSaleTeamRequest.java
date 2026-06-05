package com.dut.erp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;
import java.util.UUID;

public record UpdateSaleTeamRequest(
    @NotBlank(message = "Team name cannot be blank")
        @Size(max = 100, message = "Team name must be between 1 and 100 characters")
        String name,
    @NotNull(message = "Leader ID cannot be null") UUID leaderId,
    @NotNull(message = "Organization ID cannot be null") UUID organizationId,
    Set<UUID> memberIds) {}
