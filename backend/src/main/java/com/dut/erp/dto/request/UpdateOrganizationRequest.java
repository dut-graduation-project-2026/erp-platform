package com.dut.erp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateOrganizationRequest(
    @NotBlank(message = "Organization name is required")
        @Size(max = 255, message = "Organization name must not exceed 255 characters")
        String name,
    @Size(max = 255, message = "Description must not exceed 255 characters") String description,
    @NotBlank(message = "Address is required")
        @Size(max = 255, message = "Address must not exceed 255 characters")
        String address,
    @NotBlank(message = "Hotline is required")
        @Size(max = 255, message = "Hotline must not exceed 255 characters")
        String hotline,
    @NotBlank(message = "Tax code is required")
        @Size(max = 255, message = "Tax code must not exceed 255 characters")
        String taxCode) {}
