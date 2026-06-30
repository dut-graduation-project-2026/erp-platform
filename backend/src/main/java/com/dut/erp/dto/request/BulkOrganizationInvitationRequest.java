package com.dut.erp.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record BulkOrganizationInvitationRequest(
    @NotNull(message = "Role ID cannot be null") UUID roleId,
    @NotEmpty(message = "Emails list cannot be empty") 
    List<@Email(message = "Each entry must be a valid email address") String> emails
) {}
