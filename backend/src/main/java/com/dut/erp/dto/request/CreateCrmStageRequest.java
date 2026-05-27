package com.dut.erp.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCrmStageRequest(
    @NotBlank(message = "Stage name cannot be blank")
        @Size(max = 100, message = "Stage name must not exceed 100 characters")
        String name,
    @NotNull(message = "Sequence is required")
        @Min(value = 0, message = "Sequence must be non-negative")
        Integer sequence) {}
