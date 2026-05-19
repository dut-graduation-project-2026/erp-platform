package com.dut.erp.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCustomerContactRequest(
    @NotBlank(message = "Contact name is required")
    @Size(max = 100, message = "Contact name must not exceed 100 characters")
    String name,

    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    String email,

    @Size(max = 50, message = "Phone must not exceed 50 characters")
    String phone,

    @Size(max = 100, message = "Position must not exceed 100 characters")
    String position,

    Boolean isPrimary
) {}
