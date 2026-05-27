package com.dut.erp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSalesTeamRequest(
    @NotBlank(message = "Team name cannot be blank")
        @Size(max = 100, message = "Team name must not exceed 100 characters")
        String name,
    java.util.UUID leaderId) {}
