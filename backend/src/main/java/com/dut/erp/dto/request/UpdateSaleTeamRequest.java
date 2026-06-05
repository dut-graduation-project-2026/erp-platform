package com.dut.erp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Set;
import java.util.UUID;

public record UpdateSaleTeamRequest(
    @NotBlank(message = "Team name must not be blank")
        @Size(max = 100, message = "Team name must be between 1 and 100 characters")
        @Pattern(
            regexp = "^[a-zA-ZÀ-ỹ0-9]+(?:[ \\-'][a-zA-ZÀ-ỹ0-9]+)*$",
            message =
                "Team name must not contain leading/trailing whitespace and can only contain"
                    + " letters, numbers, spaces, hyphens, and apostrophes")
        String name,
    @NotNull(message = "Leader ID cannot be null") UUID leaderId,
    Set<UUID> memberIds) {}
