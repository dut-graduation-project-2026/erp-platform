package com.dut.erp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateOrganizationRequest(
    @NotBlank(message = "Organization name is required")
        @Size(max = 255, message = "Organization name must not exceed 255 characters")
        String name,
    @Size(max = 255, message = "Description must not exceed 255 characters") String description,
    @NotBlank(message = "Address is required")
        @Size(max = 255, message = "Address must not exceed 255 characters")
        String address,
    @NotBlank(message = "Hotline is required")
        @Size(max = 255, message = "Hotline must not exceed 255 characters")
        @Pattern(regexp = "^\\+?[0-9]{7,20}$", message = "Hotline must be a valid phone number containing only digits (7-20 characters, optional leading +)")
        String hotline,
    @NotBlank(message = "Tax code is required")
        @Size(max = 255, message = "Tax code must not exceed 255 characters")
        String taxCode) {}
