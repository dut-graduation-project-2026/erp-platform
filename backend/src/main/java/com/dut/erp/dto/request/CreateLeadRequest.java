package com.dut.erp.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateLeadRequest(
    @NotBlank(message = "Name cannot be blank")
        @Size(max = 255, message = "Name cannot exceed 255 characters")
        String name,
    @Size(max = 50, message = "Tax code cannot exceed 50 characters") String taxCode,
    @Size(max = 255, message = "Email cannot exceed 255 characters")
        @Email(message = "Email must be a valid email address")
        String email,
    @Size(max = 50, message = "Phone cannot exceed 50 characters") String phone,
    @Size(max = 255, message = "Address cannot exceed 255 characters") String address,
    @Size(max = 2000, message = "Notes cannot exceed 2000 characters") String notes,
    @NotNull(message = "Expected revenue cannot be null")
        @DecimalMin(value = "0.0", message = "Expected revenue must be at least 0")
        BigDecimal expectedRevenue,
    @NotNull(message = "Probability cannot be null")
        @DecimalMin(value = "0.0", message = "Probability must be at least 0")
        @DecimalMax(value = "100.0", message = "Probability cannot exceed 100")
        BigDecimal probability,
    @NotNull(message = "Sale Team ID cannot be null") UUID saleTeamId,
    UUID salePersonId,
    UUID partnerId) {}
