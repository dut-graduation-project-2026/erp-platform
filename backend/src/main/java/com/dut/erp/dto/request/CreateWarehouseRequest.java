package com.dut.erp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record CreateWarehouseRequest(
    @NotBlank(message = "Name cannot be blank")
        @Size(max = 255, message = "Name cannot exceed 255 characters")
        String name,
    @NotBlank(message = "Code cannot be blank")
        @Size(max = 50, message = "Code cannot exceed 50 characters")
        String code,
    @Size(max = 255, message = "Address cannot exceed 255 characters") String address,
    String description,
    @NotEmpty(message = "Staff list cannot be empty") List<UUID> staffIds,
    @NotNull(message = "Manager is required") UUID managerId) {}
