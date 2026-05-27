package com.dut.erp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record PartnerContactRequest(
    UUID id,
    @NotBlank(message = "Contact name cannot be blank")
    @Size(max = 255, message = "Contact name must not exceed 255 characters")
    String name,
    @Size(max = 255)
    String email,
    @Size(max = 50)
    String phone,
    @Size(max = 255)
    String position,
    Boolean isPrimary
) {}
