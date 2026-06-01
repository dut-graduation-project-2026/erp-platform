package com.dut.erp.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record PartnerContactRequest(
    UUID id,
    @NotBlank(message = "Contact name cannot be blank")
        @Size(max = 255, message = "Contact name must not exceed 255 characters")
        String name,
    @Size(max = 255, message = "Email must not exceed 255 characters")
        @Email(message = "Email must be a valid email address")
        String email,
    @Size(max = 50, message = "Phone must not exceed 50 characters") String phone,
    @Size(max = 255, message = "Job position must not exceed 255 characters") String jobPosition,
    @Size(max = 2000, message = "Notes must not exceed 2000 characters") String notes) {}
